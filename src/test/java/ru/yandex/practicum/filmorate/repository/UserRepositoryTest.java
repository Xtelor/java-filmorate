package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.JdbcUserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcUserRepository.class, UserRowMapper.class})
public class UserRepositoryTest {
    private final UserRepository userRepository;

    private User user;
    private User anotherUser;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .email("test@user.com")
                .login("TestLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        anotherUser = User.builder()
                .email("another@user.com")
                .login("AnotherLogin")
                .name("Another Name")
                .birthday(LocalDate.of(2000, 2, 2))
                .build();
    }

    // Проверка возвращения пустого списка пользователей при пустой БД
    @Test
    void shouldGetEmptyUserList() {
        List<User> users = userRepository.getUsers();
        assertThat(users).isEmpty();
    }

    // Проверка добавления и получения корректного пользователя
    @Test
    void shouldAddAndGetUser() {
        User savedUser = userRepository.addUser(user);
        assertThat(savedUser.getId()).isNotZero();
        Optional<User> foundUserOpt = userRepository.getUser(savedUser.getId());

        assertThat(foundUserOpt)
                .isPresent()
                .hasValueSatisfying(foundUser -> {
                    assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
                    assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
                    assertThat(foundUser.getLogin()).isEqualTo(user.getLogin());
                    assertThat(foundUser.getName()).isEqualTo(user.getName());
                    assertThat(foundUser.getBirthday()).isEqualTo(user.getBirthday());
                });
    }

    // Проверка получения списка с несколькими пользователями
    @Test
    void shouldGetUsers() {
        userRepository.addUser(user);
        userRepository.addUser(anotherUser);

        List<User> users = userRepository.getUsers();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactly("TestLogin", "AnotherLogin");
    }

    // Проверка обновления пользователя
    @Test
    void shouldUpdateUser() {
        User savedUser = userRepository.addUser(user);

        User userToUpdate = User.builder()
                .id(savedUser.getId())
                .email("updated@email.com")
                .login("UpdatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        boolean isUpdated = userRepository.updateUser(userToUpdate);
        assertThat(isUpdated).isTrue();

        Optional<User> updatedUserOpt = userRepository.getUser(savedUser.getId());
        assertThat(updatedUserOpt)
                .isPresent()
                .hasValueSatisfying(updatedUser -> {
                    assertThat(updatedUser.getLogin()).isEqualTo("UpdatedLogin");
                    assertThat(updatedUser.getEmail()).isEqualTo("updated@email.com");
                });
    }

    // Проверка возврата FALSE при попытке обновить несуществующего пользователя
    @Test
    void shouldReturnFalseWhenUpdatingNonExistentUser() {
        anotherUser.setId(999);
        boolean isUpdated = userRepository.updateUser(anotherUser);

        assertThat(isUpdated).isFalse();
    }

    // Проверка удаления пользователя
    @Test
    void shouldDeleteUser() {
        User savedUser = userRepository.addUser(user);
        assertThat(userRepository.getUser(savedUser.getId())).isPresent();

        boolean isDeleted = userRepository.deleteUser(savedUser.getId());
        assertThat(isDeleted).isTrue();

        assertThat(userRepository.getUser(savedUser.getId())).isEmpty();
    }

    // Проверка возврата FALSE при попытке удалить несуществующего пользователя
    @Test
    void shouldReturnFalseWhenDeletingNonExistentUser() {
        boolean isDeleted = userRepository.deleteUser(999);
        assertThat(isDeleted).isFalse();
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя с использованной почтой
    @Test
    void shouldNotAddUserWithDuplicateEmail() {
        userRepository.addUser(user);
        anotherUser.setEmail(user.getEmail());

        assertThatThrownBy(() -> userRepository.addUser(anotherUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка выбрасывания исключения при попытке добавить пользователя с использованным логином
    @Test
    void shouldNotAddUserWithDuplicateLogin() {
        userRepository.addUser(user);
        anotherUser.setLogin(user.getLogin()); // Устанавливаем такой же логин

        assertThatThrownBy(() -> userRepository.addUser(anotherUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка добавления в друзья и получения списка друзей
    @Test
    void shouldAddAndGetFriends() {
        User user1 = userRepository.addUser(user);
        User user2 = userRepository.addUser(anotherUser);

        // user1 добавляет в друзья user2
        userRepository.addFriend(user1.getId(), user2.getId());

        // Проверяем список друзей user1
        List<User> friendsOfUser1 = userRepository.getFriends(user1.getId());
        assertThat(friendsOfUser1)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", user2.getId());

        // У user2 пока нет друзей, поскольку дружба не взаимная
        List<User> friendsOfUser2 = userRepository.getFriends(user2.getId());
        assertThat(friendsOfUser2).isEmpty();
    }

    // Проверка удаления из друзей
    @Test
    void shouldRemoveFriend() {
        User user1 = userRepository.addUser(user);
        User user2 = userRepository.addUser(anotherUser);
        userRepository.addFriend(user1.getId(), user2.getId());

        assertThat(userRepository.getFriends(user1.getId())).isNotEmpty();

        boolean isRemoved = userRepository.removeFriend(user1.getId(), user2.getId());
        assertThat(isRemoved).isTrue();

        // Проверяем, что список друзей пуст
        assertThat(userRepository.getFriends(user1.getId())).isEmpty();
    }

    // Проверка получения списка друзей
    @Test
    void shouldGetCommonFriends() {
        User user1 = userRepository.addUser(user);
        User user2 = userRepository.addUser(anotherUser);
        User commonFriend = userRepository.addUser(User.builder()
                .email("common@friend.com")
                .login("CommonFriend")
                .name("CF")
                .birthday(LocalDate.of(1999, 9, 9))
                .build());

        userRepository.addFriend(user1.getId(), commonFriend.getId());
        userRepository.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userRepository.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", commonFriend.getId());
    }
}
