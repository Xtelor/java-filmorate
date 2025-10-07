package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenreControllerTest {

    @Mock
    private GenreService genreService;

    @InjectMocks
    private GenreController genreController;

    private Genre genre1;
    private Genre genre2;

    @BeforeEach
    void beforeEach() {
        genre1 = new Genre(1, "Комедия");
        genre2 = new Genre(2, "Драма");
    }

    // Проверка получения списка всех жанров
    @Test
    void shouldGetAllGenres() {
        when(genreService.getAll()).thenReturn(List.of(genre1, genre2));

        List<Genre> genres = genreController.getAllGenres();

        assertNotNull(genres, "Список жанров не должен быть null");
        assertEquals(2, genres.size(), "Размер списка жанров не совпадает");
        assertTrue(genres.contains(genre1), "Список должен содержать первый жанр");
        assertTrue(genres.contains(genre2), "Список должен содержать второй жанр");

        verify(genreService, times(1)).getAll();
    }

    // Проверка получения пустого списка жанров
    @Test
    void shouldGetEmptyGenreList() {
        when(genreService.getAll()).thenReturn(Collections.emptyList());

        List<Genre> genres = genreController.getAllGenres();

        assertNotNull(genres, "Список жанров не должен быть null");
        assertTrue(genres.isEmpty(), "Список жанров должен быть пустым");

        verify(genreService, times(1)).getAll();
    }

    // Проверка получения жанра по существующему ID
    @Test
    void shouldGetGenreById() {
        when(genreService.getById(1)).thenReturn(genre1);

        Genre result = genreController.getGenreById(1);

        assertNotNull(result, "Жанр не должен быть null");
        assertEquals(genre1, result, "Возвращенный жанр не совпадает с ожидаемым");
        assertEquals(1, result.getId(), "ID жанра не совпадает");

        verify(genreService, times(1)).getById(1);
    }

    // Проверка выбрасывания исключения при поиске жанра по несуществующему ID
    @Test
    void shouldThrowNotFoundExceptionWhenGenreDoesNotExist() {
        int nonExistentId = 99;
        when(genreService.getById(nonExistentId))
                .thenThrow(new NotFoundException("Жанр с id = " + nonExistentId + " не найден."));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> genreController.getGenreById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Жанр с id = " + nonExistentId + " не найден."));
        verify(genreService, times(1)).getById(nonExistentId);
    }
}