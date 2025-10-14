package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import java.util.List;

@Service
public interface UserService {
    // Добавление пользователя
    UserDto add(NewUserRequest request);

    // Получение списка всех пользователей
    List<UserDto> getAll();

    // Получение пользователя по ID
    UserDto get(int id);

    // Обновление пользователя
    UserDto update(UpdateUserRequest request);

    // Удаление пользователя по ID
    void delete(int id);

    // Удаление всех пользователей
    void deleteAll();

    // Добавление пользователя в друзья
    void addFriend(int id, int friendId);

    // Удаление пользователя из друзей
    void deleteFriend(int id, int friendId);

    // Получение списка друзей пользователей
    List<UserDto> getFriends(int id);

    // Получение списка общих друзей двух пользователей
    List<UserDto> getMutualFriends(int id, int otherId);
}
