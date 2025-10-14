package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingControllerTest {

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController ratingController;

    private Rating rating1;
    private Rating rating2;

    @BeforeEach
    void beforeEach() {
        rating1 = new Rating(1, "G");
        rating2 = new Rating(2, "PG");
    }

    // Проверка получения списка всех рейтингов
    @Test
    void shouldGetAllRatings() {
        when(ratingService.getAll()).thenReturn(List.of(rating1, rating2));

        List<Rating> ratings = ratingController.getAllRatings();

        assertNotNull(ratings, "Список рейтингов не должен быть null");
        assertEquals(2, ratings.size(), "Размер списка рейтингов не совпадает");
        assertTrue(ratings.contains(rating1), "Список должен содержать первый рейтинг");
        assertTrue(ratings.contains(rating2), "Список должен содержать второй рейтинг");

        verify(ratingService, times(1)).getAll();
    }

    // Проверка получения пустого списка рейтингов
    @Test
    void shouldGetEmptyRatingList() {
        when(ratingService.getAll()).thenReturn(Collections.emptyList());

        List<Rating> ratings = ratingController.getAllRatings();

        assertNotNull(ratings, "Список рейтингов не должен быть null");
        assertTrue(ratings.isEmpty(), "Список рейтингов должен быть пустым");

        verify(ratingService, times(1)).getAll();
    }

    // Проверка получения рейтинга по существующему ID
    @Test
    void shouldGetRatingById() {
        when(ratingService.getById(1)).thenReturn(rating1);

        Rating result = ratingController.getRatingById(1);

        assertNotNull(result, "Рейтинг не должен быть null");
        assertEquals(rating1, result, "Возвращенный рейтинг не совпадает с ожидаемым");
        assertEquals(1, result.getId(), "ID рейтинга не совпадает");

        verify(ratingService, times(1)).getById(1);
    }

    // Проверка выбрасывания исключения при поиске рейтинга по несуществующему ID
    @Test
    void shouldThrowNotFoundExceptionWhenRatingDoesNotExist() {
        int nonExistentId = 99;
        when(ratingService.getById(nonExistentId))
                .thenThrow(new NotFoundException("Рейтинг с id = " + nonExistentId + " не найден."));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> ratingController.getRatingById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Рейтинг с id = " + nonExistentId + " не найден."));
        verify(ratingService, times(1)).getById(nonExistentId);
    }
}