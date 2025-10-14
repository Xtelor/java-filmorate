package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.service.UserService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDto userDto1;
    private UserDto userDto2;
    private NewUserRequest newUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void beforeEach() {
        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("test@gmail.com");
        newUserRequest.setLogin("Admin");
        newUserRequest.setName("Jack");
        newUserRequest.setBirthday(LocalDate.of(1997, 8, 14));

        userDto1 = UserDto.builder()
                .id(1)
                .email("test@gmail.com")
                .login("Admin")
                .name("Jack")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();

        userDto2 = UserDto.builder()
                .id(2)
                .email("alex@gmail.com")
                .login("Moderator")
                .name("Alex")
                .birthday(LocalDate.of(2001, 5, 8))
                .build();

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setId(1);
        updateUserRequest.setEmail("new.email@gmail.com");
        updateUserRequest.setLogin("NewLogin");
        updateUserRequest.setName("James");
        updateUserRequest.setBirthday(LocalDate.of(1997, 8, 14));
    }

    @Test // Проверка возвращения пустого списка пользователей
    void shouldGetEmptyUserList() {
        when(userService.getAll()).thenReturn(Collections.emptyList());

        List<UserDto> users = userController.getUsers();

        assertTrue(users.isEmpty(), "Список пользователей должен быть пуст.");
        verify(userService, times(1)).getAll();
    }

    @Test // Проверка добавления корректного пользователя без заданного имени
    void shouldAddCorrectUserWithoutName() {
        newUserRequest.setName("");

        UserDto expectedDto = UserDto.builder()
                .id(1)
                .email("test@gmail.com")
                .login("Admin")
                .name("Admin") // Ожидаем, что сервис вернет логин в качестве имени
                .birthday(LocalDate.of(1997, 8, 14))
                .build();

        when(userService.add(any(NewUserRequest.class))).thenReturn(expectedDto);

        UserDto createdUser = userController.addUser(newUserRequest);

        assertEquals(expectedDto.getLogin(), createdUser.getName(), "Имя должно совпадать с логином.");
        verify(userService, times(1)).add(newUserRequest);
    }

    @Test // Проверка добавления корректного пользователя
    void shouldAddCorrectUser() {
        when(userService.add(any(NewUserRequest.class))).thenReturn(userDto1);

        UserDto createdUser = userController.addUser(newUserRequest);

        assertEquals(userDto1, createdUser, "Возвращенный пользователь не совпадает с ожидаемым.");
        verify(userService, times(1)).add(newUserRequest);
    }

    @Test // Проверка получения списка с одним пользователем
    void shouldGetUser() {
        when(userService.getAll()).thenReturn(List.of(userDto1));

        List<UserDto> users = userController.getUsers();

        assertEquals(1, users.size(), "В коллекции должен быть 1 пользователь.");
        assertTrue(users.contains(userDto1));
        verify(userService, times(1)).getAll();
    }

    @Test // Проверка получения списка с несколькими пользователями
    void shouldGetUsers() {
        when(userService.getAll()).thenReturn(List.of(userDto1, userDto2));

        List<UserDto> users = userController.getUsers();

        assertEquals(2, users.size());
        assertTrue(users.contains(userDto1));
        assertTrue(users.contains(userDto2));
        verify(userService, times(1)).getAll();
    }

    // Проверка получения пользователя по ID
    @Test
    void shouldGetUserById() {
        when(userService.get(1)).thenReturn(userDto1);

        UserDto foundUser = userController.getUser(1);

        assertEquals(userDto1, foundUser);
        verify(userService, times(1)).get(1);
    }

    // Проверка выбрасывания исключения при попытке найти несуществующего пользователя
    @Test
    void shouldThrowNotFoundExceptionWhenGetUserByNonExistentId() {
        when(userService.get(anyInt())).thenThrow(new NotFoundException("Пользователь не найден."));

        assertThrows(NotFoundException.class, () -> userController.getUser(999));
        verify(userService, times(1)).get(999);
    }

    // Проверка обновления пользователя
    @Test
    void shouldUpdateUser() {
        when(userService.update(any(UpdateUserRequest.class))).thenReturn(userDto1);

        UserDto result = userController.updateUser(updateUserRequest);

        assertEquals(userDto1, result, "Пользователь не обновился корректно");
        verify(userService, times(1)).update(updateUserRequest);
    }

    @Test // Проверка обновления пользователя без заданного имени
    void shouldUpdateUserWithoutName() {
        updateUserRequest.setName("");
        UserDto expectedDto = UserDto.builder()
                .id(1)
                .login("Admin")
                .name("Admin")
                .email("abc@gmail.com")
                .birthday(LocalDate.of(1997, 8, 14))
                .build();
        when(userService.update(any(UpdateUserRequest.class))).thenReturn(expectedDto);

        UserDto updatedUser = userController.updateUser(updateUserRequest);

        assertEquals("Admin", updatedUser.getName(), "Имя должно было быть заменено логином");
        verify(userService, times(1)).update(updateUserRequest);
    }

    @Test // Проверка выбрасывания исключения при попытке обновить несуществующего пользователя
    void shouldNotUpdateNonExistentUser() {
        when(userService.update(any(UpdateUserRequest.class))).thenThrow(new NotFoundException("Пользователь не найден"));
        assertThrows(NotFoundException.class, () -> userController.updateUser(updateUserRequest));
    }

    @Test // Проверка выбрасывания исключения при попытке обновить пользователя с используемой электронной почтой
    void shouldNotUpdateUserWithIntersectEmail() {
        when(userService.update(any(UpdateUserRequest.class))).thenThrow(new ValidationException("Эта почта уже занята"));
        assertThrows(ValidationException.class, () -> userController.updateUser(updateUserRequest));
    }
}