package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    // Добавление пользователя
    User addUser(User user);

    // Получение списка пользователей
    List<User> getUsers();

    // Получение пользователя по ID
    User getUser(int id);

    // Обновление пользователя
    User updateUser(User newUser);

    // Удаление пользователя по ID
    User deleteUser(int id);

    // Удаление всех пользователей
    void deleteUsers();
}
