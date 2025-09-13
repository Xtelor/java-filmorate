package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Comparator<Film> comparator = Comparator.comparing(
            film -> Optional.ofNullable(film.getLikes())
                    .map(Set::size)
                    .orElse(0)
    );

    // Добавление фильма
    @Override
    public Film create(Film film) {
        return filmStorage.createFilm(film);
    }

    // Обновление фильма
    @Override
    public Film update(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    // Получение фильма по ID
    @Override
    public Film get(int id) {
        return filmStorage.getFilm(id);
    }

    // Получение списка всех фильмов
    @Override
    public List<Film> getAll() {
        return filmStorage.getAllFilms();
    }

    // Удаление фильма по ID
    @Override
    public Film delete(int id) {
        return filmStorage.deleteFilm(id);
    }

    // Удаление всех фильмов
    @Override
    public void deleteAll() {
        filmStorage.deleteFilms();
    }

    // Добавление лайка
    @Override
    public void addLike(int id, int userId) {
        // Проверка корректности ID фильма
        if (id <= 0) {
            throw new ValidationException("ID фильма должен быть положительным.");
        }

        // Проверка корректности ID фильма
        if (userId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        // Получение списка ID всех пользователей
        List<Integer> usersIds = userStorage.getUsers().stream()
                .map(User::getId)
                .toList();

        // Проверка существования пользователя
        if (!usersIds.contains(userId)) {
            throw new NotFoundException("Пользователь с таким ID не найден");
        }

        // Получение фильма
        Film film = get(id);

        // Получение списка лайков фильма
        Set<Integer> likes = film.getLikes();

        // Добавление лайка
        likes.add(userId);
    }

    // Удаление лайка
    @Override
    public void deleteLike(int id, int userId) {
        // Проверка корректности ID фильма
        if (id <= 0) {
            throw new ValidationException("ID фильма должен быть положительным.");
        }

        // Проверка корректности ID фильма
        if (userId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным.");
        }

        // Получение списка ID всех пользователей
        List<Integer> usersIds = userStorage.getUsers().stream()
                .map(User::getId)
                .toList();

        // Проверка существования пользователя
        if (!usersIds.contains(userId)) {
            throw new NotFoundException("Пользователь с таким ID не найден");
        }

        // Получение фильма по ID
        Film film = get(id);

        // Получение лайков фильма
        Set<Integer> likes = film.getLikes();

        // Проверка существования лайка от пользователя
        if (!likes.contains(userId)) {
            throw new NotFoundException("Лайк пользователя с таким ID не найден.");
        }

        // Удаление лайка
        likes.remove(userId);
    }

    // Получение списка наиболее популярных фильмов по количеству лайков
    @Override
    public List<Film> getTop(int amount) {
        return new ArrayList<>(
                filmStorage.getAllFilms().stream()
                        .sorted(comparator.reversed())
                        .limit(amount)
                        .toList()
        );
    }
}
