package ru.practicum.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.util.CheckExistence;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CheckExistence objectCheckExistence;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        if (ids != null) {
            return userRepository.findByIdIn(ids, pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAll(pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new ConflictException("Email or name is required");
        }

        if (userDto.getEmail().substring(0, userDto.getEmail().indexOf("@")).length() > 64) {
            throw new ValidationException("Email localpart cannot be longer than 64 characters");
        }
        String domainPartOfEmail = userDto.getEmail().substring(userDto.getEmail().indexOf("@") + 1,
                userDto.getEmail().length() - 1);
        if (!domainPartOfEmail.contains(".")) {
            if (domainPartOfEmail.length() > 63) {
                throw new ValidationException("Email domain cannot be longer than 64 characters");
            }
        }

        Optional<User> userOpt = userRepository.findByName(userDto.getName());
        if (userOpt.isPresent()) {
            throw new ConflictException(String.format("User with name %s already exists",
                    userDto.getName()));
        }
        User user = userRepository.save(UserMapper.toUser(userDto));
        log.info("User was created [{}]", UserMapper.toUserDto(user));
        return UserMapper.toUserDto(user);
    }


    @Override
    public void deleteUser(Long id) {
        objectCheckExistence.getUser(id);
        userRepository.deleteById(id);
        log.info("User [{}] was deleted", id);
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}