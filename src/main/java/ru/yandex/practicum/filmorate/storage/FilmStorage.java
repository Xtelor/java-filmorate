package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    // Добавление фильма
    Film createFilm(Film film);

    // Получение фильма по ID
    Film getFilm(int id);

    // Обновление фильма
    Film updateFilm(Film newFilm);

    // Получение списка всех фильмов
    List<Film> getAllFilms();

    // Удаление фильма по ID
    Film deleteFilm(int id);

    // Удаление всех фильмов
    void deleteFilms();
}
