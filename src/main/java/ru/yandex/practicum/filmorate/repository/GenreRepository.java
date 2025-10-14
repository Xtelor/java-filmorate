package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository {
    // Поиск всех жанров
    List<Genre> findAll();

    // Поиск жанра по id
    Optional<Genre> findById(int id);

    // Поиск жанра по id фильма
    Set<Genre> getGenresByFilmId(int filmId);

    // Обновление жанров фильма
    void updateGenresForFilm(Film film);
}
