package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Rating;
import java.util.List;
import java.util.Optional;

public interface RatingRepository {
    // Поиск всех рейтингов
    List<Rating> findAll();

    // Поиск рейтинга по id
    Optional<Rating> findById(int id);
}
