package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmServiceImpl implements FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final RatingService ratingService;
    private final GenreService genreService;

    // Добавление фильма
    @Override
    public FilmDto create(NewFilmRequest request) {
        log.info("Запрос на создание фильма '{}'", request.getName());

        validateRatingExistence(request.getRating().getId());
        validateGenresExistence(request.getGenres());

        Film filmToCreate = FilmMapper.mapToFilm(request);
        validateFilm(filmToCreate);
        Film createdFilm = filmRepository.createFilm(filmToCreate);

        log.info("Фильм '{}' с id = {} успешно создан.", createdFilm.getName(), createdFilm.getId());

        return FilmMapper.mapToFilmDto(createdFilm);
    }

    // Обновление фильма
    @Override
    public FilmDto update(UpdateFilmRequest request) {
        log.info("Запрос на обновление фильма с id = {}", request.getId());
        // Проверка существования фильма
        validateFilmExistence(request.getId());
        validateRatingExistence(request.getRating().getId());
        validateGenresExistence(request.getGenres());

        Film filmToUpdate = FilmMapper.mapToFilm(request);
        validateFilm(filmToUpdate);
        filmRepository.updateFilm(filmToUpdate);

        int likesCount = filmRepository.getLikesCount(filmToUpdate.getId());
        log.info("Фильм с id = {} успешно обновлен.", request.getId());

        return FilmMapper.mapToFilmDto(filmToUpdate, likesCount);
    }

    // Получение фильма по ID
    @Override
    public FilmDto get(int id) {
        log.info("Запрос на получение фильма с id = {}", id);

        // Проверка существования фильма
        Film film = filmRepository.getFilm(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден."));

        // Получение количества лайков фильма
        int likesCount = filmRepository.getLikesCount(id);

        return FilmMapper.mapToFilmDto(film, likesCount);
    }

    // Получение списка всех фильмов
    @Override
    public List<FilmDto> getAll() {
        log.info("Запрос на получение списка всех фильмов.");
        List<Film> films = filmRepository.getAllFilms();

        return films.stream()
                .map(film -> {
                    int likesCount = filmRepository.getLikesCount(film.getId());
                    return FilmMapper.mapToFilmDto(film, likesCount);
                })
                .collect(Collectors.toList());
    }

    // Удаление фильма по ID
    @Override
    public void delete(int id) {
        log.info("Запрос на удаление фильма с id = {}", id);

        // Проверка существования фильма
        validateFilmExistence(id);
        filmRepository.deleteFilm(id);

        log.info("Фильм с id = {} успешно удален.", id);
    }

    // Удаление всех фильмов
    @Override
    public void deleteAll() {
        log.info("Запрос на удаление всех фильмов.");

        filmRepository.deleteFilms();

        log.info("Все фильмы удалены.");
    }

    // Добавление лайка
    @Override
    public void addLike(int id, int userId) {
        log.info("Пользователь с id = {} ставит лайк фильму с id = {}", userId, id);

        // Проверка существования фильма и пользователя
        validateFilmExistence(id);
        validateUserExistence(userId);

        filmRepository.addLike(id, userId);

        log.info("Лайк успешно добавлен.");
    }

    // Удаление лайка
    @Override
    public void deleteLike(int id, int userId) {
        log.info("Пользователь с id = {} удаляет лайк у фильма с id = {}", userId, id);

        // Проверка существования фильма и пользователя
        validateFilmExistence(id);
        validateUserExistence(userId);

        if (!filmRepository.removeLike(id, userId)) {
            log.warn("Попытка удалить несуществующий лайк от user с id = {} для фильма с id={}", userId, id);
            throw new NotFoundException("Лайк для удаления не найден");
        } else {
            log.info("Лайк успешно удален.");
        }
    }

    // Получение списка наиболее популярных фильмов по количеству лайков
    @Override
    public List<FilmDto> getTop(int amount) {
        log.info("Запрос на получение топ-{} фильмов.", amount);
        List<Film> topFilms = filmRepository.getMostPopularFilms(amount);

        return topFilms.stream()
                .map(film -> {
                    int likesCount = filmRepository.getLikesCount(film.getId());
                    return FilmMapper.mapToFilmDto(film, likesCount);
                })
                .collect(Collectors.toList());
    }

    // Валидация фильма
    private void validateFilm(Film film) {
        // название не может быть пустым
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым.");
        }

        // максимальная длина описания — 200 символов
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не должно превышать 200 символов.");
        }

        // дата релиза — не раньше 28 декабря 1895 года
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }

        // продолжительность фильма должна быть положительным числом
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    // Проверка существования фильма
    private void validateFilmExistence(int filmId) {
        if (filmRepository.getFilm(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }

    // Проверка существования пользователя
    private void validateUserExistence(int userId) {
        if (userRepository.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
    }

    // Проверка существования всех жанров
    private void validateGenresExistence(Set<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            for (Genre genre : genres) {
                genreService.getById(genre.getId());
            }
        }
    }

    // Проверка существования рейтинга
    private void validateRatingExistence(int id) {
        ratingService.getById(id);
    }
}
