package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Выполнение метода getUsers.");
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Начало выполнения метода addUser.");

        validateUser(user);
        log.info("Пройдена валидация пользователя в методе addUser.");

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Автоматически заполнено пустое имя пользователя.");
        }

        user.setId(getNextId());
        log.info("Пользователю присвоен ID.");

        users.put(user.getId(), user);
        log.info("Завершение выполнения метода addUser.");

        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Начало выполнения метода updateUser.");

        if (!users.containsKey(newUser.getId())) {
            log.info("Ошибка обновления: пользователь с таким ID не найден.");
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        validateUser(newUser);
        log.info("Пройдена валидация пользователя в методе updateUser.");

        User oldUser = users.get(newUser.getId());

        if (users.values()
                .stream()
                .filter(user -> user.getId() != newUser.getId())
                .anyMatch(user -> Objects.equals(user.getEmail(), newUser.getEmail()))
        ) {
            log.error("Ошибка валидации: электронная почта уже используется.");
            throw new ValidationException("Эта электронная почта уже используется.");
        }

        oldUser.setEmail(newUser.getEmail());
        log.info("Обновлена электронная почта пользователя.");

        String oldLogin = oldUser.getLogin();

        oldUser.setLogin(newUser.getLogin());
        log.info("Обновлен логин пользователя.");

        oldUser.setBirthday(newUser.getBirthday());
        log.info("Обновлен день рождения пользователя.");

        if (newUser.getName() != null && !newUser.getName().isEmpty()) {
            oldUser.setName(newUser.getName());
            log.info("Обновлено имя пользователя.");
        } else {
            if (Objects.equals(oldUser.getName(), oldLogin)) {
                oldUser.setName(newUser.getLogin());
                log.info("Автоматически обновлено имя пользователя.");
            }
        }

        log.info("Завершение выполнения метода updateUser.");
        return oldUser;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.error("Ошибка валидации: электронная почта пустая.");
            throw new ValidationException("Электронная почта не может быть пустой.");
        }

        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: отсутствует символ @ в электронной почте.");
            throw new ValidationException("Электронная почта должна содержать символ @.");
        }

        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин пустой или содержит пробелы.");
            throw new ValidationException("Логин не может быть пустым или содержать пробелы.");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения пользователя в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

    }
}
