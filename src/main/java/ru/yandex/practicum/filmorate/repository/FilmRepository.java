package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;
import java.util.Optional;

public interface FilmRepository {
    // Добавление фильма
    Film createFilm(Film film);

    // Получение фильма по ID
    Optional<Film> getFilm(int id);

    // Обновление фильма
    boolean updateFilm(Film newFilm);

    // Получение списка всех фильмов
    List<Film> getAllFilms();

    // Удаление фильма по ID
    boolean deleteFilm(int id);

    // Удаление всех фильмов
    void deleteFilms();

    // Добавление лайка
    void addLike(int filmId, int userId);

    // Удаление лайка. Возвращает true, если лайк был найден и удален
    boolean removeLike(int filmId, int userId);

    // Получение количества лайков для фильма
    int getLikesCount(int filmId);

    // Получение списка самых популярных фильмов.
    List<Film> getMostPopularFilms(int count);
}
