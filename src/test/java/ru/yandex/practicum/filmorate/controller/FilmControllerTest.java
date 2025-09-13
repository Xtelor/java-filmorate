package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmServiceImpl;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;


import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private final FilmController filmController = new FilmController(
            new FilmServiceImpl(
                    new InMemoryFilmStorage(),
                    new InMemoryUserStorage()
            )
    );
    private Film film;
    private Film anotherFilm;

    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        film = Film.builder()
                .name("Чужой")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1979, 6, 22))
                .duration(116)
                .build();
    }

    @Test
        // Проверка возвращения пустого списка фильмов
    void shouldGetEmptyFilmList() {
        assertTrue(filmController.getFilms().isEmpty(), "Список фильмов должен быть пуст");
    }

    @Test
        // Проверка добавления корректного фильма
    void shouldAddCorrectFilm() {
        Film receivedFilm = filmController.addFilm(film);

        assertEquals(film.getId(), receivedFilm.getId(), "Неверный ID фильма");
        assertEquals(film.getName(), receivedFilm.getName(), "Названия фильмов отличаются");
        assertEquals(film.getDescription(), receivedFilm.getDescription(), "Описание фильмов отличается");
        assertEquals(film.getReleaseDate(), receivedFilm.getReleaseDate(), "Дата релиза отличается");
        assertEquals(film.getDuration(), receivedFilm.getDuration(), "Длительность отличается");
    }

    @Test
        // Проверка добавления фильма с описанием, длина которого составляет 200 символов
    void shouldAddFilmWithLongDescription() {
        String description = "OK".repeat(100);
        film.setDescription(description);
        Film receivedFilm = filmController.addFilm(film);
        assertEquals(description, receivedFilm.getDescription(), "Описания не совпадают");
    }

    @Test
        // Проверка получения списка с одним фильмом
    void shouldGetFilm() {
        filmController.addFilm(film);
        Collection<Film> films = filmController.getFilms();

        assertFalse(films.isEmpty(), "Коллекция не должна быть пустой");
        assertEquals(1, films.size(), "В коллекции должен быть 1 фильм");
        assertTrue(films.contains(film), "В коллекции нет нужного фильма");
    }

    @Test
        // Проверка получения списка с несколькими фильмами
    void shouldGetFilms() {
        anotherFilm = Film.builder()
                .name("Чужой 2")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1994, 3, 25))
                .duration(137)
                .build();

        filmController.addFilm(film);
        filmController.addFilm(anotherFilm);

        Collection<Film> films = filmController.getFilms();

        assertFalse(films.isEmpty(), "Коллекция не должна быть пустой");
        assertEquals(2, films.size(), "В коллекции должен быть 2 фильма");
        assertTrue(films.contains(film), "В коллекции нет нужного фильма");
        assertTrue(films.contains(anotherFilm), "В коллекции нет нужного фильма");
    }

    @Test
        // Проверка невозможности создания фильма без названия
    void shouldNotCreateFilmWithoutName() {
        film = Film.builder()
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1979, 6, 22))
                .duration(116)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Название фильма не может быть пустым.")));
    }

    @Test
        // Проверка выбрасывания исключения при попытке добавить фильм с пустым названием
    void shouldNotAddFilmWithBlankName() {
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Название фильма не может быть пустым.")));
    }

    @Test
        // Проверка невозможности создания фильма без описания
    void shouldNotAddFilmWithoutDescription() {
        film = Film.builder()
                .name("Чужой")
                .releaseDate(LocalDate.of(1979, 6, 22))
                .duration(116)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Описание фильма не может быть пустым.")));
    }

    @Test
        // Проверка выбрасывания исключения при попытке добавить фильм с пустым описанием
    void shouldNotAddFilmWithBlankDescription() {
        film.setDescription("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Описание фильма не может быть пустым.")));
    }

    @Test
        // Проверка выбрасывания исключения при попытке добавить фильм со слишком длинным описанием
    void shouldNotAddFilmWithTooLongDescription() {
        film.setDescription("OK".repeat(100) + ".");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Максимальная длина описания фильма — 200 символов.")));
    }

    @Test
        // Проверка невозможности создания фильма без даты релиза
    void shouldNotCreateFilmWithoutReleaseDate() {
        film = Film.builder()
                .name("Чужой")
                .description("Фантастика, Ужасы")
                .duration(116)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Дата релиза не может быть пустой.")));
    }

    @Test
        // Проверка выбрасывания исключения при попытке добавить фильм с датой релиза раньше 28 декабря 1895 года
    void shouldNotAddFilmWithIncorrectReleaseDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Дата релиза — не раньше 28 декабря 1895 года.")));
    }

    @Test
        // Проверка невозможности создания фильма без длительности
    void shouldNotCreateFilmWithoutDuration() {
        film = Film.builder()
                .name("Чужой")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1979, 6, 22))
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Продолжительность фильма не может быть пустой.")));
    }

    @Test
        // Проверка выбрасывания исключения при попытке добавить фильм с нулевой длительностью
    void shouldNotAddFilmWithZeroDuration() {
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Продолжительность фильма должна быть положительным числом.")));
    }

    @Test
        // Проверка выбрасывания исключения при попытке добавить фильм с отрицательной длительностью
    void shouldNotAddFilmWithNegativeDuration() {
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(v -> v.getMessage()
                        .equals("Продолжительность фильма должна быть положительным числом.")));
    }

    @Test
        // Проверка корректности обновления фильма
    void shouldUpdateFilm() {
        filmController.addFilm(film);

        anotherFilm = Film.builder()
                .id(film.getId())
                .name("Чужой 2")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1994, 3, 25))
                .duration(137)
                .build();

        Film updatedFilm = filmController.updateFilm(anotherFilm);
        assertEquals(anotherFilm, updatedFilm, "Фильм не обновился");
    }

    @Test
        // Проверка обновления фильма на фильм с описанием, длина которого составляет 200 символов
    void shouldUpdateFilmWithLongDescription() {
        filmController.addFilm(film);

        anotherFilm = Film.builder()
                .id(film.getId())
                .name("Чужой 2")
                .description("OK".repeat(100))
                .releaseDate(LocalDate.of(1994, 3, 25))
                .duration(137)
                .build();

        Film updatedFilm = filmController.updateFilm(anotherFilm);
        assertEquals(anotherFilm, updatedFilm, "Фильм не обновился");
    }

    @Test
        // Проверка выбрасывания исключения при попытке обновить несуществующий фильм
    void shouldNotUpdateNonExistentFilm() {
        filmController.addFilm(film);

        anotherFilm = Film.builder()
                .id(3)
                .name("Чужой 2")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1994, 3, 25))
                .duration(137)
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.updateFilm(anotherFilm),
                "Исключение при попытке обновить несуществующий фильм"
        );

        assertEquals("Фильм с id = " + anotherFilm.getId() + "не найден.", exception.getMessage());
    }
}
