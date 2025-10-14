package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    // Создание пользователя
    @Override
    public UserDto add(NewUserRequest request) {
        log.info("Запрос на создание пользователя '{}'", request.getName());

        User user = UserMapper.mapToUser(request);

        // Валидация пользователя
        validateUser(user);
        validateUserName(user);

        User createdUser = userRepository.addUser(user);

        log.info("Пользователь '{}' с id = {} успешно создан.", createdUser.getName(), createdUser.getId());

        return UserMapper.mapToUserDto(createdUser);
    }

    // Получение списка всех пользователей
    @Override
    public List<UserDto> getAll() {
        log.info("Запрос на получение списка всех пользователей.");

        return userRepository.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Получение пользователя по его ID
    @Override
    public UserDto get(int id) {
        log.info("Запрос на получение пользователя с id = {}", id);

        User user = userRepository.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден."));

        return UserMapper.mapToUserDto(user);
    }

    // Обновление пользователя
    @Override
    public UserDto update(UpdateUserRequest request) {
        log.info("Запрос на обновление пользователя с id = {}", request.getId());

        // Проверка существования пользователя и его валидация
        validateExistence(request.getId());
        User user = UserMapper.mapToUser(request);
        validateUser(user);
        validateUserName(user);
        userRepository.updateUser(user);

        log.info("Пользователь с id = {} успешно обновлен.", request.getId());

        return UserMapper.mapToUserDto(user);
    }

    // Удаление пользователя по ID
    @Override
    public void delete(int id) {
        log.info("Запрос на удаление пользователя с id = {}", id);

        // Проверка существования пользователя
        validateExistence(id);
        userRepository.deleteUser(id);

        log.info("Пользователь с id = {} успешно удален.", id);
    }

    // Удаление всех пользователей
    @Override
    public void deleteAll() {
        log.info("Запрос на удаление всех пользователей.");

        userRepository.deleteUsers();

        log.info("Все пользователи удалены.");
    }

    // Добавление пользователя в список друзей
    @Override
    public void addFriend(int id, int friendId) {
        log.info("Пользователь с id = {} добавляет в друзья пользователя с id = {}", id, friendId);

        // Проверка на добавление в друзья самого себя
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить в друзья самого себя.");
        }

        // Проверка существования пользователей
        validateExistence(id, friendId);

        userRepository.addFriend(id, friendId);
        log.info("Пользователь успешно добавлен в друзья.");
    }

    // Удаление пользователя из друзей
    @Override
    public void deleteFriend(int id, int friendId) {
        log.info("Пользователь с id = {} удаляет из друзей пользователя с id = {}", id, friendId);

        if (id == friendId) {
            throw new ValidationException("Нельзя удалить из друзей самого себя.");
        }

        // Проверка существования пользователей
        validateExistence(id, friendId);

        userRepository.removeFriend(id, friendId);
        log.info("Пользователь успешно удален из друзей.");
    }

    // Получение списка друзей пользователя
    @Override
    public List<UserDto> getFriends(int id) {
        log.info("Запрос на получение списка друзей пользователя с id = {}", id);

        // Проверка существования пользователей
        validateExistence(id);

        return userRepository.getFriends(id).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Получение списка общих друзей пользователей
    @Override
    public List<UserDto> getMutualFriends(int id, int otherId) {
        log.info("Запрос на получение общего списка друзей пользователей с id = {},{} ", id, otherId);

        if (id == otherId) {
            throw new ValidationException("Нельзя получить список общих друзей с самим собой.");
        }

        // Проверка существования пользователей
        validateExistence(id, otherId);

        return userRepository.getCommonFriends(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Метод автозаполнения имени
    private void validateUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя с логином '{}' не задано. " +
                    "В качестве имени будет использоваться логин.", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    // Метод валидации пользователя
    private void validateUser(User user) {
        // электронная почта не может быть пустой и должна содержать символ @
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не может быть пустой.");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ @.");
        }

        // логин не может быть пустым и содержать пробелы
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы.");
        }

        // дата рождения не может быть в будущем
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }

    // Метод проверки существования пользователя/пользователей
    private void validateExistence(int... ids) {
        for (int id : ids) {
            if (userRepository.getUser(id).isEmpty()) {
                throw new NotFoundException("Пользователь с id=" + id + " не найден.");
            }
        }
    }
}
