package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> getFilms() {
        log.info("Выполнение метода getFilms.");
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable @Positive int id) {
        log.info("Выполнение метода getFilm.");
        return filmService.get(id);
    }

    @PostMapping
    public FilmDto addFilm(@Valid @RequestBody NewFilmRequest request) {
        log.info("Выполнение метода addFilm.");
        return filmService.create(request);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody UpdateFilmRequest request) {
        log.info("Выполнение метода updateFilm.");
        return filmService.update(request);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable @Positive int id) {
        log.info("Выполнение метода deleteFilm.");
        filmService.delete(id);
    }

    @DeleteMapping
    public void deleteFilms() {
        log.info("Выполнение метода deleteFilms.");
        filmService.deleteAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive int id,
                        @PathVariable @Positive int userId) {
        log.info("Выполнение метода addLike.");
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Positive int id,
                           @PathVariable @Positive int userId) {
        log.info("Выполнение метода deleteLike.");
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getTop(@RequestParam(defaultValue = "10") @Min(1) int amount) {
        log.info("Выполнение метода getTop.");
        return filmService.getTop(amount);
    }
}
