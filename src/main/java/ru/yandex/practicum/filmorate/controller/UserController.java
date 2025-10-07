package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Выполнение метода getUsers.");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable @Positive int id) {
        log.info("Выполнение метода getUser.");
        return userService.get(id);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody NewUserRequest request) {
        log.info("Выполнение метода addUser.");
        return userService.add(request);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Выполнение метода updateUser.");
        return userService.update(request);
    }

    @DeleteMapping
    public void deleteUsers() {
        log.info("Выполнение метода deleteUsers.");
        userService.deleteAll();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Positive int id) {
        log.info("Выполнение метода deleteUser.");
        userService.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive int id,
                          @PathVariable @Positive int friendId) {
        log.info("Выполнение метода addFriend.");
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable @Positive int id,
                             @PathVariable @Positive int friendId) {
        log.info("Выполнение метода deleteFriend.");
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable @Positive int id) {
        log.info("Выполнение метода getFriends.");
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getMutualFriends(@PathVariable @Positive int id,
                                          @PathVariable @Positive int otherId) {
        log.info("Выполнение метода getMutualFriends.");
        return userService.getMutualFriends(id, otherId);
    }
}
