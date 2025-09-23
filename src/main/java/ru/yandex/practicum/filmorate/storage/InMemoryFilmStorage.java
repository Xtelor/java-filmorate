package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    // Добавление фильма
    @Override
    public Film createFilm(Film film) {
        // Фильму присваивается ID
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    // Получение фильма по ID
    @Override
    public Film getFilm(int id) {
        // Проверка существования фильма
        if (!films.containsKey(id)) {
            throw new NotFoundException("Ошибка получения: фильм с заданным ID не найден.");
        }

        return films.get(id);
    }

    // Обновление фильма
    @Override
    public Film updateFilm(Film newFilm) {
        // Проверка существования фильма
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + "не найден.");
        }

        // Получение фильма для обновления
        Film oldFilm = films.get(newFilm.getId());

        // Обновление названия фильма
        oldFilm.setName(newFilm.getName());
        // Обновление описания фильма
        oldFilm.setDescription(newFilm.getDescription());
        // Обновление даты релиза фильма
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        // Обновление длительности фильма
        oldFilm.setDuration(newFilm.getDuration());

        return oldFilm;
    }

    // Получение списка всех фильмов
    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // Удаление фильма по ID
    @Override
    public Film deleteFilm(int id) {
        // Проверка существования фильма
        if (!films.containsKey(id)) {
            throw new NotFoundException("Ошибка удаления: фильм с заданным ID не найден.");
        }

        return films.remove(id);
    }

    // Удаление всех фильмов
    @Override
    public void deleteFilms() {
        films.clear();
    }

    // Получение уникального ID для следующего фильма
    private int getNextId() {
        // Получение наибольшего ID на момент вызова метода
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
