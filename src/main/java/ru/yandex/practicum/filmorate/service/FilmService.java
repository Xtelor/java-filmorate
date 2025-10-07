package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import java.util.List;

public interface FilmService {
    // Добавление фильма
    FilmDto create(NewFilmRequest request);

    // Обновление фильма
    FilmDto update(UpdateFilmRequest request);

    // Получение фильма по ID
    FilmDto get(int id);

    // Получение списка всех фильмов
    List<FilmDto> getAll();

    // Удаление фильма по ID
    void delete(int id);

    // Удаление всех фильмов
    void deleteAll();

    // Добавление лайка
    void addLike(int id, int userId);

    // Удаление лайка
    void deleteLike(int id, int userId);

    // Получение списка наиболее популярных фильмов по количеству лайков
    List<FilmDto> getTop(int amount);
}
