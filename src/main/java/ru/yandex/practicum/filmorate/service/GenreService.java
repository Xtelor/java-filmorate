package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;

public interface GenreService {
    // Получение всех жанров
    public List<Genre> getAll();

    // Получение жанра по id
    public Genre getById(int id);
}
