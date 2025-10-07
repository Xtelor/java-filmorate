package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.RatingRepository;
import ru.yandex.practicum.filmorate.service.impl.RatingServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

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
        when(ratingRepository.findAll()).thenReturn(List.of(rating1, rating2));

        List<Rating> result = ratingService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(rating1));
        verify(ratingRepository, times(1)).findAll();
    }

    // Проверка получения пустого списка
    @Test
    void shouldGetEmptyList() {
        when(ratingRepository.findAll()).thenReturn(Collections.emptyList());

        List<Rating> result = ratingService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ratingRepository, times(1)).findAll();
    }

    // Проверка получения рейтинга по ID
    @Test
    void shouldGetRatingById() {
        when(ratingRepository.findById(1)).thenReturn(Optional.of(rating1));

        Rating result = ratingService.getById(1);

        assertNotNull(result);
        assertEquals(rating1.getId(), result.getId());
        assertEquals(rating1.getName(), result.getName());
        verify(ratingRepository, times(1)).findById(1);
    }

    // Проверка выбрасывания исключения при попытке получить по ID несуществующий рейтинг
    @Test
    void shouldThrowNotFoundExceptionWhenRatingDoesNotExist() {
        when(ratingRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ratingService.getById(99));
        verify(ratingRepository, times(1)).findById(99);
    }
}