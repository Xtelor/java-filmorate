package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.service.GenreService;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    // Получение списка всех жанров
    @Override
    public List<Genre> getAll() {
        log.info("Запрос на получение списка всех жанров.");
        return genreRepository.findAll();
    }

    // Получение жанра по его ID
    @Override
    public Genre getById(int id) {
        log.info("Запрос на получение жанра с id = {}.", id);
        return genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден."));
    }
}
