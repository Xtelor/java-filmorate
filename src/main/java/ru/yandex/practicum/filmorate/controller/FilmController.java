package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Выполнение метода getFilms.");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Начало выполнения метода addFilm.");

        film.setId(getNextId());
        log.info("Фильму присвоен ID.");

        films.put(film.getId(), film);
        log.info("Завершение выполнения метода addFilm.");
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Начало выполнения метода updateFilm.");

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
}
