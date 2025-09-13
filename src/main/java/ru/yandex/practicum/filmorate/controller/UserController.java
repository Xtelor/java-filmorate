package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Выполнение метода getUsers.");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        log.info("Выполнение метода getUser.");
        return userService.get(id);
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Выполнение метода addUser.");
        return userService.add(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Выполнение метода updateUser.");
        return userService.update(newUser);
    }

    @DeleteMapping
    public void deleteUsers() {
        log.info("Выполнение метода deleteUsers.");
        userService.deleteAll();
    }

    @DeleteMapping("/{id}")
    public User deleteUser(@PathVariable int id) {
        log.info("Выполнение метода deleteUser.");
        return userService.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Выполнение метода addFriend.");
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Выполнение метода deleteFriend.");
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("Выполнение метода getFriends.");
        return userService.getFriends(id);
    }

    @GetMapping("{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Выполнение метода getMutualFriends.");
        return userService.getMutualFriends(id, otherId);
    }
}
