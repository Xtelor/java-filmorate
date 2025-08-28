package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private final UserController userController = new UserController();
    private User user;
    private User anotherUser;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .email("abc@gmail.com")
                .login("Admin")
                .name("")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();
    }

    @Test // Проверка возвращения пустого списка пользователей
    void shouldGetEmptyFilmList() {
        assertTrue(userController.getUsers().isEmpty(), "Список пользователей должен быть пуст");
    }

    @Test // Проверка добавления корректного пользователя без заданного имени
    void shouldAddCorrectUserWithoutName() {
        User receivedUser = userController.addUser(user);

        assertEquals(user.getId(), receivedUser.getId(), "Неверный ID пользователя");
        assertEquals(user.getEmail(), receivedUser.getEmail(), "Электронные почты отличаются");
        assertEquals(user.getLogin(), receivedUser.getLogin(), "Логины отличаются");
        assertEquals(user.getName(), receivedUser.getName(), "Имена отличаются");
        assertEquals(user.getBirthday(), receivedUser.getBirthday(), "Дни рождения отличаются");
    }

    @Test // Проверка добавления корректного пользователя
    void shouldAddCorrectUser() {
        user.setName("Jack");
        User receivedUser = userController.addUser(user);

        assertEquals(user.getId(), receivedUser.getId(), "Неверный ID пользователя");
        assertEquals(user.getEmail(), receivedUser.getEmail(), "Электронные почты отличаются");
        assertEquals(user.getLogin(), receivedUser.getLogin(), "Логины отличаются");
        assertEquals(user.getName(), receivedUser.getName(), "Имена отличаются");
        assertEquals(user.getBirthday(), receivedUser.getBirthday(), "Дни рождения отличаются");
    }

    @Test // Проверка получения списка с одним пользователем
    void shouldGetUser() {
        userController.addUser(user);
        Collection<User> users = userController.getUsers();

        assertFalse(users.isEmpty(), "Коллекция не должна быть пустой");
        assertEquals(1, users.size(), "В коллекции должен быть 1 пользователь");
        assertTrue(users.contains(user), "Пользователя нет в коллекции");
    }

    @Test // Проверка получения списка с несколькими пользователями
    void shouldGetUsers() {
        anotherUser = User.builder()
                .email("mod@gmail.com")
                .login("Moderator").name("Alex")
                .birthday(LocalDate.of(2001, 5, 8))
                .build();

        userController.addUser(user);
        userController.addUser(anotherUser);

        Collection<User> users = userController.getUsers();

        assertFalse(users.isEmpty(), "Коллекция не должна быть пустой");
        assertEquals(2, users.size(), "В коллекции должен быть 2 пользователя");
        assertTrue(users.contains(user), "Пользователя нет в коллекции");
        assertTrue(users.contains(anotherUser), "Пользователя нет в коллекции");
    }

    @Test // Проверка выбрасывания исключения при попытке создать пользователя без логина
    void shouldNotCreateUserWithoutLogin() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->  user = User.builder()
                        .email("abc@gmail.com")
                        .name("")
                        .birthday(LocalDate.of(1997, 8, 14))
                        .build(),
                "Исключение при попытке создать пользователя без логина"
        );

        assertEquals("login is marked non-null but is null", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке добавить пользователя с пустым логином
    void shouldNotAddUserWithEmptyLogin() {
        user.setLogin("");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () ->  userController.addUser(user),
                "Исключение при попытке создать пользователя без логина"
        );

        assertEquals("Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке добавить пользователя с логином, содержащим пробелы
    void shouldNotAddUserWithIncorrectLogin() {
        user.setLogin("a dmi n");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () ->  userController.addUser(user),
                "Исключение при попытке создать пользователя c некорректным логином"
        );

        assertEquals("Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке добавить пользователя без электронной почты
    void shouldNotAddUserWithoutEmail() {
        user.setEmail(null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () ->  userController.addUser(user),
                "Исключение при попытке создать пользователя без электронной почты"
        );

        assertEquals("Электронная почта не может быть пустой.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке добавить пользователя с пустой электронной почтой
    void shouldNotAddUserWithEmptyEmail() {
        user.setEmail("");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () ->  userController.addUser(user),
                "Исключение при попытке создать пользователя без электронной почты"
        );

        assertEquals("Электронная почта не может быть пустой.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке добавить пользователя с некорректной электронной почтой
    void shouldNotAddUserWithIncorrectEmail() {
        user.setEmail("abcgmail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () ->  userController.addUser(user),
                "Исключение при попытке создать пользователя c электронной почтой без @"
        );

        assertEquals("Электронная почта должна содержать символ @.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке создать пользователя без дня рождения
    void shouldNotCreateUserWithoutBirthday() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->  user = User.builder()
                        .email("abc@gmail.com")
                        .login("Admin")
                        .name("")
                        .build(),
                "Исключение при попытке создать пользователя без дня рождения"
        );

        assertEquals("birthday is marked non-null but is null", exception.getMessage());
    }

    @Test// Проверка выбрасывания исключения при попытке создать пользователя с днем рождения в будущем
    void shouldNotAddUserWithIncorrectBirthday() {
        user.setBirthday(LocalDate.of(2077,12,12));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () ->  userController.addUser(user),
                "Исключение при попытке добавить пользователя c днем рождения в будущем"
        );

        assertEquals("Дата рождения не может быть в будущем.", exception.getMessage());
    }

    @Test // Проверка обновления пользователя без заданного имени
    void shouldUpdateUserWithoutName() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("abcd@gmail.com")
                .login("Adm")
                .name("")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();

        User updatedUser = userController.updateUser(anotherUser);

        assertEquals(1, userController.getUsers().size(), "Пользователь не добавлен");
        assertEquals(user, updatedUser, "Пользователь не обновлен");
        assertEquals(anotherUser.getEmail(), updatedUser.getEmail(), "Электронные почты отличаются");
        assertEquals(anotherUser.getLogin(), updatedUser.getLogin(), "Логины отличаются");
        assertEquals(anotherUser.getLogin(), updatedUser.getName(), "Имена отличаются");
        assertEquals(anotherUser.getBirthday(), updatedUser.getBirthday(), "Дни рождения отличаются");
    }

    @Test // Проверка обновления пользователя
    void shouldUpdateUser() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("abcde@gmail.com")
                .login("Mod")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();

        User updatedUser = userController.updateUser(anotherUser);

        assertEquals(1, userController.getUsers().size(), "Пользователь не добавлен");
        assertEquals(user, updatedUser, "Пользователь не обновлен");
        assertEquals(anotherUser.getEmail(), updatedUser.getEmail(), "Электронные почты отличаются");
        assertEquals(anotherUser.getLogin(), updatedUser.getLogin(), "Логины отличаются");
        assertEquals(anotherUser.getName(), updatedUser.getName(), "Имена отличаются");
        assertEquals(anotherUser.getBirthday(), updatedUser.getBirthday(), "Дни рождения отличаются");
    }

    @Test // Проверка выбрасывания исключения при попытке обновить несуществующего пользователя
    void shouldNotUpdateNonExistentUser() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(33)
                .email("abc@gmail.com")
                .login("Mod")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();


        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить несуществующего пользователя"
        );
        assertEquals("Пользователь с id = " + anotherUser.getId() + " не найден.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с пустой электронной почтой
    void shouldNotUpdateUserWithBlankEmail() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("")
                .login("Mod")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить пользователя с пустой электронной почтой"
        );
        assertEquals("Электронная почта не может быть пустой.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с некорректной электронной почтой
    void shouldNotUpdateUserWithIncorrectEmail() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("fbcgmail.com")
                .login("Mod")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить пользователя с некорректной электронной почтой"
        );
        assertEquals("Электронная почта должна содержать символ @.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с используемой электронной почтой
    void shouldNotUpdateUserWithIntersectEmail() {
        userController.addUser(user);

        User secondUser = User.builder()
                .email("abcd@gmail.com")
                .login("Mod")
                .name("Joe")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();

        userController.addUser(secondUser);

        anotherUser = User.builder()
                .id(user.getId())
                .email("abcd@gmail.com")
                .login("Adm")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить пользователя с использованной электронной почтой"
        );
        assertEquals("Эта электронная почта уже используется.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с пустым логином
    void shouldNotUpdateUserWithBlankLogin() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("abc@gmail.com")
                .login("")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить пользователя с пустым логином"
        );
        assertEquals("Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с некорректным логином
    void shouldNotUpdateUserWithIncorrectLogin() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("abc@gmail.com")
                .login("A d min")
                .name("James")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить пользователя с некорректным логином"
        );
        assertEquals("Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с некорректным днем рождения
    void shouldNotUpdateUserWithIncorrectBirthday() {
        userController.addUser(user);

        anotherUser = User.builder()
                .id(user.getId())
                .email("abc@gmail.com")
                .login("Admin")
                .name("James")
                .birthday(LocalDate.of(2026, 8, 14))
                .build();


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(anotherUser),
                "Исключение при попытке обновить пользователя с некорректным днем рождения"
        );
        assertEquals("Дата рождения не может быть в будущем.", exception.getMessage());
    }
}
