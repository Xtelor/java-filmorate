package ru.yandex.practicum.filmorate.service;

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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.impl.UserServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private NewUserRequest newUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void beforeEach() {
        user1 = User.builder()
                .id(1)
                .email("user1@example.com")
                .login("user1_login")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        user2 = User.builder()
                .id(2)
                .email("user2@example.com")
                .login("user2_login")
                .name("User Two")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();

        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("user1@example.com");
        newUserRequest.setLogin("user1_login");
        newUserRequest.setName("User One");
        newUserRequest.setBirthday(LocalDate.of(1990, 1, 1));

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setId(1);
        updateUserRequest.setEmail("user1.updated@example.com");
        updateUserRequest.setLogin("user1_updated_login");
        updateUserRequest.setName("User One Updated");
        updateUserRequest.setBirthday(LocalDate.of(1990, 1, 1));
    }

    // Проверка успешного создания пользователя
    @Test
    void shouldAddUserSuccessfully() {
        when(userRepository.addUser(any(User.class))).thenReturn(user1);

        UserDto result = userService.add(newUserRequest);

        assertNotNull(result);
        assertEquals(user1.getId(), result.getId());
        assertEquals(newUserRequest.getName(), result.getName());
        verify(userRepository, times(1)).addUser(any(User.class));
    }

    // Проверка установки логина в качестве имени, если имя пустое
    @Test
    void shouldSetLoginAsNameWhenNameIsEmptyOnAdd() {
        newUserRequest.setName("");
        User userWithLoginAsName = user1;
        userWithLoginAsName.setName(user1.getLogin());

        when(userRepository.addUser(any(User.class))).thenReturn(userWithLoginAsName);

        UserDto result = userService.add(newUserRequest);

        assertEquals(user1.getLogin(), result.getName());
        verify(userRepository, times(1)).addUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя без электронной почты
    @Test
    void shouldThrowValidationExceptionWhenAddingUserWithBlankEmail() {
        newUserRequest.setEmail("");

        assertThrows(ValidationException.class, () -> userService.add(newUserRequest));
        verify(userRepository, never()).addUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя с некорректной почтой
    @Test
    void shouldThrowValidationExceptionWhenAddingUserWithIncorrectEmail() {
        newUserRequest.setEmail("user.example.com");

        assertThrows(ValidationException.class, () -> userService.add(newUserRequest));
        verify(userRepository, never()).addUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя без логина
    @Test
    void shouldThrowValidationExceptionWhenAddingUserWithBlankLogin() {
        newUserRequest.setLogin("");

        assertThrows(ValidationException.class, () -> userService.add(newUserRequest));
        verify(userRepository, never()).addUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя с некорректным логином
    @Test
    void shouldThrowValidationExceptionWhenAddingUserWithLoginContainingSpace() {
        newUserRequest.setLogin("user login");

        assertThrows(ValidationException.class, () -> userService.add(newUserRequest));
        verify(userRepository, never()).addUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя с некорректным днем рождения
    @Test
    void shouldThrowValidationExceptionWhenAddingUserWithFutureBirthday() {
        newUserRequest.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userService.add(newUserRequest));
        verify(userRepository, never()).addUser(any(User.class));
    }

    // Проверка получения списка всех пользователей
    @Test
    void shouldGetAllUsers() {
        when(userRepository.getUsers()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.getFirst().getId());
    }

    // Проверка получения пустого списка пользователей
    @Test
    void shouldGetEmptyUserList() {
        when(userRepository.getUsers()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAll();

        assertTrue(result.isEmpty());
    }

    // Проверка получения пользователя по ID
    @Test
    void shouldGetUserById() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        UserDto result = userService.get(1);

        assertEquals(user1.getName(), result.getName());
        verify(userRepository, times(1)).getUser(1);
    }

    // Проверка выбрасывания исключения при попытке получить несуществующего пользователя
    @Test
    void shouldThrowNotFoundExceptionWhenGettingNonExistentUser() {
        when(userRepository.getUser(990)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.get(990));
    }

    // Проверка успешного обновления пользователя
    @Test
    void shouldUpdateUserSuccessfully() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.updateUser(any(User.class))).thenReturn(true);

        UserDto result = userService.update(updateUserRequest);

        assertEquals(updateUserRequest.getName(), result.getName());
        verify(userRepository, times(1)).updateUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке обновить несуществующего пользователя
    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentUser() {
        when(userRepository.getUser(999)).thenReturn(Optional.empty());
        updateUserRequest.setId(999);

        assertThrows(NotFoundException.class, () -> userService.update(updateUserRequest));
        verify(userRepository, never()).updateUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке обновить почту на пустую
    @Test
    void shouldThrowValidationExceptionWhenUpdatingUserWithBlankEmail() {
        updateUserRequest.setEmail("");
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        assertThrows(ValidationException.class, () -> userService.update(updateUserRequest));
        verify(userRepository, never()).updateUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке обновить почту на некорректную
    @Test
    void shouldThrowValidationExceptionWhenUpdatingUserWithIncorrectEmail() {
        updateUserRequest.setEmail("user.example.com");
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        assertThrows(ValidationException.class, () -> userService.update(updateUserRequest));
        verify(userRepository, never()).updateUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке обновить логин на пустой
    @Test
    void shouldThrowValidationExceptionWhenUpdatingUserWithBlankLogin() {
        updateUserRequest.setLogin(" ");
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        assertThrows(ValidationException.class, () -> userService.update(updateUserRequest));
        verify(userRepository, never()).updateUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке обновить логин на некорректный
    @Test
    void shouldThrowValidationExceptionWhenUpdatingUserWithLoginContainingSpace() {
        updateUserRequest.setLogin("user login");
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        assertThrows(ValidationException.class, () -> userService.update(updateUserRequest));
        verify(userRepository, never()).updateUser(any(User.class));
    }

    // Проверка выбрасывания исключения при попытке обновить день рождения на некорректный
    @Test
    void shouldThrowValidationExceptionWhenUpdatingUserWithFutureBirthday() {
        updateUserRequest.setBirthday(LocalDate.now().plusDays(1));
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        assertThrows(ValidationException.class, () -> userService.update(updateUserRequest));
        verify(userRepository, never()).updateUser(any(User.class));
    }

    // Проверка установки логина в качестве имени при обновлении
    @Test
    void shouldSetLoginAsNameWhenNameIsNullOnUpdate() {
        updateUserRequest.setName(null);
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.updateUser(any(User.class))).thenReturn(true);

        UserDto result = userService.update(updateUserRequest);

        assertEquals(updateUserRequest.getLogin(), result.getName());
        verify(userRepository, times(1)).updateUser(any(User.class));
    }

    // Проверка успешного удаления пользователя по ID
    @Test
    void shouldDeleteUserById() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));

        userService.delete(1);

        verify(userRepository, times(1)).deleteUser(1);
    }

    // Проверка выбрасывания исключения при попытке удалить несуществующего пользователя
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
        when(userRepository.getUser(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(999));
    }

    // Проверка удаления всех пользователей
    @Test
    void shouldDeleteAllUsers() {
        doNothing().when(userRepository).deleteUsers();

        userService.deleteAll();

        verify(userRepository, times(1)).deleteUsers();
    }

    // Проверка успешного добавления в друзья
    @Test
    void shouldAddFriendSuccessfully() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getUser(2)).thenReturn(Optional.of(user2));

        userService.addFriend(1, 2);

        verify(userRepository, times(1)).addFriend(1, 2);
    }

    // Проверка выбрасывания исключения при попытке добавить в друзья самого себя
    @Test
    void shouldThrowValidationExceptionWhenAddingSelfAsFriend() {
        assertThrows(ValidationException.class, () -> userService.addFriend(1, 1));
        verify(userRepository, never()).addFriend(anyInt(), anyInt());
    }

    // Проверка выбрасывания исключения при добавлении друга, если пользователь не найден
    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExistForAddFriend() {
        when(userRepository.getUser(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(999, 1));
    }

    // Проверка выбрасывания исключения при добавлении несуществующего друга
    @Test
    void shouldThrowNotFoundExceptionWhenFriendDoesNotExistForAddFriend() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getUser(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addFriend(1, 999));

        verify(userRepository, never()).addFriend(anyInt(), anyInt());
    }

    // Проверка успешного удаления друга
    @Test
    void shouldDeleteFriendSuccessfully() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getUser(2)).thenReturn(Optional.of(user2));

        userService.deleteFriend(1, 2);

        verify(userRepository, times(1)).removeFriend(1, 2);
    }

    // Проверка выбрасывания исключения при попытке удалить из друзей самого себя
    @Test
    void shouldThrowValidationExceptionWhenDeletingSelfAsFriend() {
        assertThrows(ValidationException.class, () -> userService.deleteFriend(1, 1));
        verify(userRepository, never()).removeFriend(anyInt(), anyInt());
    }

    // Проверка выбрасывания исключения при удалении друга, если пользователь не найден
    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExistForDeleteFriend() {
        when(userRepository.getUser(999)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(999, 1));
        verify(userRepository, never()).removeFriend(anyInt(), anyInt());
    }

    // Проверка выбрасывания исключения при удалении несуществующего друга
    @Test
    void shouldThrowNotFoundExceptionWhenFriendDoesNotExistForDeleteFriend() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getUser(999)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(1, 999));
        verify(userRepository, never()).removeFriend(anyInt(), anyInt());
    }

    // Проверка получения списка друзей пользователя
    @Test
    void shouldGetFriends() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getFriends(1)).thenReturn(List.of(user2));

        List<UserDto> result = userService.getFriends(1);

        assertEquals(1, result.size());
        assertEquals(user2.getId(), result.getFirst().getId());
    }

    // Проверка получения пустого списка друзей пользователя
    @Test
    void shouldGetEmptyFriendsList() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getFriends(1)).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getFriends(1);

        assertTrue(result.isEmpty());
    }

    // Проверка получения списка общих друзей пользователей
    @Test
    void shouldGetMutualFriends() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getUser(2)).thenReturn(Optional.of(user2));
        User mutualFriend = User.builder().id(3).build();
        when(userRepository.getCommonFriends(1, 2)).thenReturn(List.of(mutualFriend));

        List<UserDto> result = userService.getMutualFriends(1, 2);

        assertEquals(1, result.size());
        assertEquals(mutualFriend.getId(), result.getFirst().getId());
    }

    @Test
    void shouldGetEmptyMutualFriendsList() {
        when(userRepository.getUser(1)).thenReturn(Optional.of(user1));
        when(userRepository.getUser(2)).thenReturn(Optional.of(user2));
        when(userRepository.getCommonFriends(1, 2)).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getMutualFriends(1, 2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).getCommonFriends(1, 2);
    }

    // Проверка выбрасывания исключения при поиске общих друзей с самим собой
    @Test
    void shouldThrowValidationExceptionWhenGettingMutualFriendsWithSelf() {
        assertThrows(ValidationException.class, () -> userService.getMutualFriends(1, 1));
    }
}
