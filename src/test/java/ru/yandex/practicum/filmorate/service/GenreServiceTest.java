package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.service.impl.GenreServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

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
        when(genreRepository.findAll()).thenReturn(List.of(genre1, genre2));

        List<Genre> result = genreService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(genre1));
        verify(genreRepository, times(1)).findAll();
    }

    // Проверка получения пустого списка
    @Test
    void shouldGetEmptyList() {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        List<Genre> result = genreService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(genreRepository, times(1)).findAll();
    }

    // Проверка получения жанра по ID
    @Test
    void shouldGetGenreById() {
        when(genreRepository.findById(1)).thenReturn(Optional.of(genre1));

        Genre result = genreService.getById(1);

        assertNotNull(result);
        assertEquals(genre1.getId(), result.getId());
        assertEquals(genre1.getName(), result.getName());
        verify(genreRepository, times(1)).findById(1);
    }

    // Проверка выбрасывания исключения при попытке получить по ID несуществующий жанр
    @Test
    void shouldThrowNotFoundExceptionWhenGenreDoesNotExist() {
        when(genreRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> genreService.getById(99));
        verify(genreRepository, times(1)).findById(99);
    }
}