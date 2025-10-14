package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // Добавление пользователя
    User addUser(User user);

    // Получение списка пользователей
    List<User> getUsers();

    // Получение пользователя по ID
    Optional<User> getUser(int id);

    // Обновление пользователя
    boolean updateUser(User newUser);

    // Удаление пользователя по ID
    boolean deleteUser(int id);

    // Удаление всех пользователей
    void deleteUsers();

    // Добавление пользователя в друзья
    void addFriend(int userId, int friendId);

    // Удаление пользователя из друзей
    boolean removeFriend(int userId, int friendId);

    // Получение списка друзей пользователя
    List<User> getFriends(int userId);

    // Получение списка общих друзей пользователей
    List<User> getCommonFriends(int userId, int otherId);
}
