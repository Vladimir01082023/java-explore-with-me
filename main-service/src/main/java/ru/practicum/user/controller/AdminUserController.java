package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> get(@RequestParam(required = false) List<Long> ids,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GETTING ALL USERS");
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserShortDto userShortDto) {
        log.info("CREATING ALL USERS");
        return userService.createUser(userShortDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("DELETING USER");
        userService.deleteUser(userId);
    }
}
