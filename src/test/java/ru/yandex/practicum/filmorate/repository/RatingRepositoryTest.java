package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.impl.JdbcRatingRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(JdbcRatingRepository.class)
public class RatingRepositoryTest {
    private final RatingRepository ratingRepository;

    // Проверка нахождения всех рейтингов
    @Test
    void shouldFindAllRatings() {
        List<Rating> ratings = ratingRepository.findAll();

        assertThat(ratings)
                .hasSize(5)
                .extracting(Rating::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    // Проверка нахождения существующего рейтинга по ID
    @Test
    void shouldFindRatingById() {
        Optional<Rating> ratingOptional = ratingRepository.findById(3);

        assertThat(ratingOptional)
                .isPresent()
                .hasValueSatisfying(rating ->
                        assertThat(rating)
                                .hasFieldOrPropertyWithValue("id", 3)
                                .hasFieldOrPropertyWithValue("name", "PG-13")
                );
    }

    // Проверка возвращения пустого значения при попытке найти несуществующих рейтинг
    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<Rating> ratingOptional = ratingRepository.findById(999);

        assertThat(ratingOptional).isEmpty();
    }
}
