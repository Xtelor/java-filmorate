package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    // Добавление фильма
    Film create(Film film);

    // Обновление фильма
    Film update(Film newFilm);

    // Получение фильма по ID
    Film get(int id);

    // Получение списка всех фильмов
    List<Film> getAll();

    // Удаление фильма по ID
    Film delete(int id);

    // Удаление всех фильмов
    void deleteAll();

    // Добавление лайка
    void addLike(int id, int userId);

    // Удаление лайка
    void deleteLike(int id, int userId);

    // Получение списка наиболее популярных фильмов по количеству лайков
    List<Film> getTop(int amount);
}
