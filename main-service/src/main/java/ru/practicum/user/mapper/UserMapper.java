package ru.practicum.user.mapper;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

public class UserMapper {
    public static UserShortDto toUserShortDto(User user) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setEmail(user.getEmail());
        userShortDto.setName(user.getName());
        return userShortDto;
    }

    public static User toUser(UserShortDto userShortDto) {
        User user = new User();
        user.setEmail(userShortDto.getEmail());
        user.setName(userShortDto.getName());
        return user;
    }

    public static UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        return userDto;
    }
}
