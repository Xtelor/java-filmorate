package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
public interface UserService {
    // Добавление пользователя
    User add(User user);

    // Получение списка всех пользователей
    List<User> getAll();

    // Получение пользователя по ID
    User get(int id);

    // Обновление пользователя
    User update(User newUser);

    // Удаление пользователя по ID
    User delete(int id);

    // Удаление всех пользователей
    void deleteAll();

    // Добавление пользователя в друзья
    void addFriend(int id, int friendId);

    // Удаление пользователя из друзей
    void deleteFriend(int id, int friendId);

    // Получение списка друзей пользователей
    List<User> getFriends(int id);

    // Получение списка общих друзей двух пользователей
    List<User> getMutualFriends(int id, int otherId);
}
