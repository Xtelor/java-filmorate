package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Выполнение метода getFilms.");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Начало выполнения метода addFilm.");

        validateFilm(film);
        log.info("Пройдена валидация фильма в методе addFilm.");

        film.setId(getNextId());
        log.info("Фильму присвоен ID.");

        films.put(film.getId(), film);
        log.info("Завершение выполнения метода addFilm.");
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Начало выполнения метода updateFilm.");

        validateFilm(newFilm);
        log.info("Пройдена валидация фильма в методе updateFilm.");

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setName(newFilm.getName());
            log.info("Обновлено название фильма.");

            oldFilm.setDescription(newFilm.getDescription());
            log.info("Обновлено описание фильма.");

            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.info("Обновлена дата релиза фильма.");

            oldFilm.setDuration(newFilm.getDuration());
            log.info("Обновлена длительность фильма");

            log.info("Завершение выполнения метода updateFilm.");
            return oldFilm;
        }

        log.info("Ошибка обновления: фильм с таким ID не найден.");
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + "не найден.");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        if (film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое.");
            throw new ValidationException("Название фильма не может быть пустым.");
        }

        if (film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма слишком длинное.");
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }

        if (film.getDescription().isBlank()) {
            log.error("Ошибка валидации: описание фильма пустое.");
            throw  new ValidationException("Описание фильма не может быть пустым.");
        }

        if (film.getReleaseDate()
                .isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: некорректная дата релиза.");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года.");
        }

        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: некорректная продолжительность фильма.");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }
}
