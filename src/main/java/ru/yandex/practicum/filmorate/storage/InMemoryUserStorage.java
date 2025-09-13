package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    // Добавление пользователя
    @Override
    public User addUser(User user) {
        // Сохранение логина пользователя в качестве его имени при отсутствии последнего
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

        // Получение пользователем ID
        user.setId(getNextId());

        users.put(user.getId(), user);
        return user;
    }

    // Получение списка всех пользователей
    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    // Получение пользователя по ID
    @Override
    public User getUser(int id) {
        // Проверка существования пользователя
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }

        return users.get(id);
    }

    // Обновление пользователя
    @Override
    public User updateUser(User newUser) {
        // Проверка существования пользователя
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        // Получение пользователя для обновления
        User oldUser = users.get(newUser.getId());

        // Проверка доступности электронной почты перед её обновлением
        if (users.values()
                .stream()
                .filter(user -> user.getId() != newUser.getId())
                .anyMatch(user -> Objects.equals(user.getEmail(), newUser.getEmail()))
        ) {
            throw new ValidationException("Эта электронная почта уже используется.");
        }

        // Обновление электронной почты пользователя
        oldUser.setEmail(newUser.getEmail());
        // Получение старого логина
        String oldLogin = oldUser.getLogin();
        // Обновление логина пользователя
        oldUser.setLogin(newUser.getLogin());
        // Обновление даты рождения пользователя
        oldUser.setBirthday(newUser.getBirthday());

        // Обновление имени пользователя
        if (newUser.getName() != null && !newUser.getName().isEmpty()) {
            oldUser.setName(newUser.getName());
        } else {
            if (Objects.equals(oldUser.getName(), oldLogin)) {
                oldUser.setName(newUser.getLogin());
            }
        }

        return oldUser;
    }

    // Удаление пользователя по ID
    @Override
    public User deleteUser(int id) {
        // Проверка существования пользователя
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }

        return users.remove(id);
    }

    // Удаление всех пользователей
    @Override
    public void deleteUsers() {
        users.clear();
    }

    // Получение уникального ID для следующего фильма
    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
