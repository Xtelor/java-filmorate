package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilmControllerTest {

    @Mock
    private FilmService filmService;

    @InjectMocks
    private FilmController filmController;

    private FilmDto filmDto1;
    private FilmDto filmDto2;
    private NewFilmRequest newFilmRequest;
    private UpdateFilmRequest updateFilmRequest;

    @BeforeEach
    void beforeEach() {
        Rating mpaG = new Rating(1, "G");
        Genre dramaGenre = new Genre(2, "Драма");
        Set<Genre> genres = Set.of(dramaGenre);

        newFilmRequest = new NewFilmRequest();
        newFilmRequest.setName("Чужой");
        newFilmRequest.setDescription("Фантастика, Ужасы");
        newFilmRequest.setReleaseDate(LocalDate.of(1979, 6, 22));
        newFilmRequest.setDuration(116);
        newFilmRequest.setRating(mpaG);
        newFilmRequest.setGenres(genres);

        filmDto1 = FilmDto.builder()
                .id(1)
                .name("Чужой")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1979, 6, 22))
                .duration(116)
                .likesCount(0)
                .rating(mpaG)
                .genres(genres)
                .build();

        filmDto2 = FilmDto.builder()
                .id(2)
                .name("Чужой 2")
                .description("Фантастика, Боевик")
                .releaseDate(LocalDate.of(1986, 7, 18))
                .duration(137)
                .likesCount(0)
                .rating(mpaG)
                .genres(genres)
                .build();

        updateFilmRequest = new UpdateFilmRequest();
        updateFilmRequest.setId(1);
        updateFilmRequest.setName("Чужой (Режиссерская версия)");
        updateFilmRequest.setDescription("Полная версия культового фильма");
        updateFilmRequest.setReleaseDate(LocalDate.of(1979, 6, 22));
        updateFilmRequest.setDuration(124);
        updateFilmRequest.setRating(mpaG);
        updateFilmRequest.setGenres(genres);
    }

    // Проверка возвращения пустого списка фильмов
    @Test
    void shouldGetEmptyFilmList() {
        when(filmService.getAll()).thenReturn(Collections.emptyList());

        List<FilmDto> films = filmController.getFilms();

        assertNotNull(films, "Список фильмов не должен быть null");
        assertTrue(films.isEmpty(), "Список фильмов должен быть пуст");
        verify(filmService, times(1)).getAll();
    }

    // Проверка добавления корректного фильма
    @Test
    void shouldAddCorrectFilm() {
        when(filmService.create(newFilmRequest)).thenReturn(filmDto1);

        FilmDto createdFilm = filmController.addFilm(newFilmRequest);

        assertEquals(filmDto1, createdFilm, "Возвращенный фильм не совпадает с ожидаемым");
        assertEquals(1, createdFilm.getId(), "ID не совпадает");
        assertEquals("Чужой", createdFilm.getName(), "Название не совпадает");
        verify(filmService, times(1)).create(newFilmRequest);
    }

    // Проверка добавления фильма с описанием, длина которого составляет 200 символов
    @Test
    void shouldAddFilmWithLongDescription() {
        String description = "A".repeat(200);
        newFilmRequest.setDescription(description);
        filmDto1.setDescription(description);

        when(filmService.create(newFilmRequest)).thenReturn(filmDto1);

        FilmDto createdFilm = filmController.addFilm(newFilmRequest);

        assertEquals(description, createdFilm.getDescription());
        verify(filmService, times(1)).create(newFilmRequest);
    }

    // Проверка получения списка с одним фильмом
    @Test
    void shouldGetFilm() {
        when(filmService.getAll()).thenReturn(List.of(filmDto1));

        List<FilmDto> films = filmController.getFilms();

        assertFalse(films.isEmpty(), "Коллекция не должна быть пустой");
        assertEquals(1, films.size(), "В коллекции должен быть 1 фильм");
        assertTrue(films.contains(filmDto1), "В коллекции нет нужного фильма");
        verify(filmService, times(1)).getAll();
    }

    // Проверка получения списка с несколькими фильмами
    @Test
    void shouldGetFilms() {
        when(filmService.getAll()).thenReturn(List.of(filmDto1, filmDto2));

        List<FilmDto> films = filmController.getFilms();

        assertFalse(films.isEmpty(), "Коллекция не должна быть пустой");
        assertEquals(2, films.size(), "В коллекции должно быть 2 фильма");
        assertTrue(films.contains(filmDto1), "В коллекции нет первого фильма");
        assertTrue(films.contains(filmDto2), "В коллекции нет второго фильма");
        verify(filmService, times(1)).getAll();
    }

    // Проверка корректности обновления фильма
    @Test
    void shouldUpdateFilm() {
        FilmDto updatedDto = FilmDto.builder()
                .id(updateFilmRequest.getId())
                .name(updateFilmRequest.getName())
                .description(updateFilmRequest.getDescription())
                .releaseDate(updateFilmRequest.getReleaseDate())
                .duration(updateFilmRequest.getDuration())
                .rating(updateFilmRequest.getRating())
                .genres(updateFilmRequest.getGenres())
                .build();

        when(filmService.update(updateFilmRequest)).thenReturn(updatedDto);

        FilmDto result = filmController.updateFilm(updateFilmRequest);

        assertEquals(updatedDto, result, "Фильм не обновился корректно");
        assertEquals(updateFilmRequest.getName(), result.getName());
        verify(filmService, times(1)).update(updateFilmRequest);
    }

    // Проверка обновления фильма на фильм с описанием, длина которого составляет 200 символов
    @Test
    void shouldUpdateFilmWithLongDescription() {
        String longDescription = "B".repeat(200);
        updateFilmRequest.setDescription(longDescription);

        FilmDto expectedDto = FilmDto.builder()
                .id(updateFilmRequest.getId())
                .name(updateFilmRequest.getName())
                .description(longDescription)
                .build();

        when(filmService.update(any(UpdateFilmRequest.class))).thenReturn(expectedDto);

        FilmDto result = filmController.updateFilm(updateFilmRequest);

        assertEquals(longDescription, result.getDescription(), "Описание не обновилось");
        verify(filmService, times(1)).update(updateFilmRequest);
    }

    // Проверка выбрасывания исключения при попытке обновить несуществующий фильм
    @Test
    void shouldNotUpdateNonExistentFilm() {
        when(filmService.update(any(UpdateFilmRequest.class)))
                .thenThrow(new NotFoundException("Фильм с id = " + updateFilmRequest.getId() + " не найден."));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.updateFilm(updateFilmRequest)
        );

        assertTrue(exception.getMessage().contains("Фильм с id = " + updateFilmRequest.getId() + " не найден."));
        verify(filmService, times(1)).update(updateFilmRequest);
    }
}
