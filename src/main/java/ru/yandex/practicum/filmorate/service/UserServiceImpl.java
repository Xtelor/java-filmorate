package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    // Добавление пользователя
    @Override
    public User add(User user) {
        return userStorage.addUser(user);
    }

    // Получение списка всех пользователей
    @Override
    public List<User> getAll() {
        return userStorage.getUsers();
    }

    // Получение пользователя по ID
    @Override
    public User get(int id) {
        return userStorage.getUser(id);
    }

    // Обновление пользователя
    @Override
    public User update(User newUser) {
        return userStorage.updateUser(newUser);
    }

    // Удаление пользователя по ID
    @Override
    public User delete(int id) {
        return userStorage.deleteUser(id);
    }

    // Удаление всех пользователей
    @Override
    public void deleteAll() {
        userStorage.deleteUsers();
    }

    // Добавление пользователя в список друзей
    @Override
    public void addFriend(int id, int friendId) {
        // Проверка корректности ID пользователей
        if (id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        if (friendId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        // Получение списков друзей двух пользователей
        Set<Integer> firstUserFriends = get(id).getFriends();
        Set<Integer> secondUserFriends = get(friendId).getFriends();

        // Взаимное добавление пользователей в друзья
        firstUserFriends.add(friendId);
        secondUserFriends.add(id);
    }

    // Удаление пользователя из друзей
    @Override
    public void deleteFriend(int id, int friendId) {
        // Проверка корректности ID пользователей
        if (id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        if (friendId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        // Получение списков друзей двух пользователей
        Set<Integer> firstUserFriends = get(id).getFriends();
        Set<Integer> secondUserFriends = get(friendId).getFriends();

        // Взаимное удаление из списков друзей
        firstUserFriends.remove(friendId);
        secondUserFriends.remove(id);
    }

    // Получение списка друзей
    @Override
    public List<User> getFriends(int id) {
        // Проверка корректности ID пользователя
        if (id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        // Возвращение списка друзей
        return get(id).getFriends().stream()
                .map(this::get)
                .toList();
    }

    // Получение списка общих друзей двух пользователей
    @Override
    public List<User> getMutualFriends(int id, int otherId) {
        // Проверка корректности ID пользователей
        if (id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        if (otherId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        // Получение списков друзей пользователей
        List<User> firstUserFriends = getFriends(id);
        List<User> secondUserFriends = getFriends(otherId);

        // Возврат списка общих друзей
        return firstUserFriends.stream()
                .filter(secondUserFriends::contains)
                .collect(Collectors.toList());
    }
}
