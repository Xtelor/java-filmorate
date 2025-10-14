package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {
    // Преобразование из модели в DTO с лайками
    public static FilmDto mapToFilmDto(Film film, int likesCount) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .rating(film.getRating())
                .genres(film.getGenres())
                .likesCount(likesCount)
                .build();
    }

    // Преобразование из модели в DTO без лайков
    public static FilmDto mapToFilmDto(Film film) {
        return mapToFilmDto(film, 0);
    }

    // Преобразование в модель из DTO для создания
    public static Film mapToFilm(NewFilmRequest request) {
        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .rating(request.getRating())
                .genres(sortGenres(request.getGenres()))
                .build();
    }

    // Преобразование в модель из DTO для обновления
    public static Film mapToFilm(UpdateFilmRequest request) {
        return Film.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .rating(request.getRating())
                .genres(sortGenres(request.getGenres()))
                .build();
    }

    // Метод для сортировки жанров
    private static Set<Genre> sortGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return Collections.emptySet();
        }

        return genres.stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
