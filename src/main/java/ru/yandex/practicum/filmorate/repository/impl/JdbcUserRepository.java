package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository extends BaseRepository<User> implements UserRepository {
    // 1. ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ
    private static final String INSERT_USER_SQL = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (:email, :login, :name, :birthday)";
    // 2. ПОЛУЧЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
    private static final String SELECT_ALL_USERS_SQL = "SELECT * FROM users ORDER BY id";
    // 3. ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ID
    private static final String SELECT_USER_BY_ID_SQL = "SELECT * FROM users WHERE id = :id";
    // 4. ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ
    private static final String UPDATE_USER_SQL = "UPDATE users " +
            "SET email = :email, login = :login, name = :name, " +
            "birthday = :birthday WHERE id = :id";
    // 5. УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ID
    private static final String DELETE_USER_BY_ID_SQL = "DELETE FROM users WHERE id = :id";
    // 6. УДАЛЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
    private static final String DELETE_ALL_USERS_SQL = "DELETE FROM users";
    // 7. ДОБАВЛЕНИЕ В ДРУЗЬЯ
    private static final String INSERT_FRIEND_SQL = "INSERT INTO friendship (user_id, friend_id) " +
            "VALUES (:userId, :friendId)";
    // 8. УДАЛЕНИЕ ИЗ ДРУЗЕЙ
    private static final String DELETE_FRIEND_SQL = "DELETE FROM friendship " +
            "WHERE user_id = :userId AND friend_id = :friendId";
    // 9. ПОЛУЧЕНИЕ СПИСКА ДРУЗЕЙ ПОЛЬЗОВАТЕЛЯ
    private static final String SELECT_FRIENDS_SQL = "SELECT u.* FROM users u JOIN friendship " +
            "f ON u.id = f.friend_id " +
            "WHERE f.user_id = :userId";
    // 10. ПОЛУЧЕНИЕ СПИСКА ОБЩИХ ДРУЗЕЙ ДВУХ ПОЛЬЗОВАТЕЛЕЙ
    private static final String SELECT_COMMON_FRIENDS_SQL = "SELECT u.* FROM users u " +
            "JOIN friendship f1 ON u.id = f1.friend_id " +
            "JOIN friendship f2 ON u.id = f2.friend_id " +
            "WHERE f1.user_id = :userId AND f2.user_id = :otherId";

    @Autowired
    public JdbcUserRepository(NamedParameterJdbcOperations jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    // Добавление пользователя
    @Override
    public User addUser(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());

        int id = insert(INSERT_USER_SQL, params);
        user.setId(id);
        return user;
    }

    // Получение списка пользователей
    @Override
    public List<User> getUsers() {
        return findAll(SELECT_ALL_USERS_SQL);
    }

    // Получение пользователя по id
    @Override
    public Optional<User> getUser(int id) {
        return findOne(SELECT_USER_BY_ID_SQL, new MapSqlParameterSource("id", id));
    }

    // Обновление пользователя
    @Override
    public boolean updateUser(User newUser) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", newUser.getId())
                .addValue("email", newUser.getEmail())
                .addValue("login", newUser.getLogin())
                .addValue("name", newUser.getName())
                .addValue("birthday", newUser.getBirthday());

        return update(UPDATE_USER_SQL, params) > 0;
    }

    // Удаление пользователя по id
    @Override
    public boolean deleteUser(int id) {
        return update(DELETE_USER_BY_ID_SQL,
                new MapSqlParameterSource("id", id)) > 0;
    }

    // Удаление всех пользователей
    @Override
    public void deleteUsers() {
        update(DELETE_ALL_USERS_SQL);
    }

    // Создание заявки в друзья
    @Override
    public void addFriend(int userId, int friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        update(INSERT_FRIEND_SQL, params);
    }

    // Удаление пользователя из друзей
    @Override
    public boolean removeFriend(int userId, int friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return update(DELETE_FRIEND_SQL, params) > 0;
    }

    // Получение списка друзей пользователя
    @Override
    public List<User> getFriends(int userId) {
        return findAll(SELECT_FRIENDS_SQL, new MapSqlParameterSource("userId", userId));
    }

    // Получение списка общих друзей двух пользователей
    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherId", otherId);

        return findAll(SELECT_COMMON_FRIENDS_SQL, params);
    }
}
