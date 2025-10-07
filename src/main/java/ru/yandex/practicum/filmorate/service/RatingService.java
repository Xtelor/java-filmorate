package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Rating;
import java.util.List;

public interface RatingService {
    // Получение всех жанров
    public List<Rating> getAll();

    // Получение жанра по id
    public Rating getById(int id);
}
