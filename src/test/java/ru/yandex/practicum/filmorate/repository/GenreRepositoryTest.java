package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.impl.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcRatingRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.RatingRowMapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcFilmRepository.class, JdbcGenreRepository.class, JdbcRatingRepository.class,
        GenreRowMapper.class, FilmRowMapper.class, RatingRowMapper.class})
public class GenreRepositoryTest {
    private final GenreRepository genreRepository;
    private final FilmRepository filmRepository;

    private Film film;

    @BeforeEach
    void beforeEach() {
        film = Film.builder()
                .name("Тестовый фильм.")
                .description("Описание.")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .rating(new Rating(1, "G"))
                .build();
    }

    // Проверка нахождения всех жанров
    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreRepository.findAll();

        assertThat(genres)
                .hasSize(6)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    // Проверка нахождения жанра по ID
    @Test
    void shouldFindGenreById() {
        Optional<Genre> genreOptional = genreRepository.findById(2);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre)
                                .hasFieldOrPropertyWithValue("id", 2)
                                .hasFieldOrPropertyWithValue("name", "Драма")
                );
    }

    // Проверка возвращения пустого значения при попытке получить несуществующий жанр по ID
    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<Genre> genreOptional = genreRepository.findById(999);

        assertThat(genreOptional).isEmpty();
    }

    // Получение жанров фильма
    @Test
    void shouldGetGenresByFilmId() {
        film.setGenres(new LinkedHashSet<>(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма"))));
        Film savedFilm = filmRepository.createFilm(film);
        Set<Genre> genres = genreRepository.getGenresByFilmId(savedFilm.getId());

        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма");
    }

    // Проверка возвращения пустого списка для фильма без жанров
    @Test
    void shouldReturnEmptySetForFilmWithNoGenres() {
        film.setGenres(new LinkedHashSet<>());
        Film savedFilm = filmRepository.createFilm(film);
        Set<Genre> genres = genreRepository.getGenresByFilmId(savedFilm.getId());

        assertThat(genres).isEmpty();
    }

    // Проверка корректности обновления жанров при увеличении их количества
    @Test
    void shouldUpdateGenresForFilm_FromNoneToSome() {
        filmRepository.createFilm(film);
        assertThat(genreRepository.getGenresByFilmId(film.getId())).isEmpty();

        Set<Genre> newGenres = Set.of(new Genre(4, "Триллер"), new Genre(6, "Боевик"));
        film.setGenres(new LinkedHashSet<>(newGenres));
        genreRepository.updateGenresForFilm(film);


        Set<Genre> updatedGenres = genreRepository.getGenresByFilmId(film.getId());
        assertThat(updatedGenres)
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactly(4, 6);
    }

    // Проверка корректности обновления жанров при уменьшении их количества
    @Test
    void shouldUpdateGenresForFilm_FromSomeToFewer() {
        film.setGenres(new LinkedHashSet<>(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма"), new Genre(6, "Боевик"))));
        Film savedFilm = filmRepository.createFilm(film);

        savedFilm.setGenres(new LinkedHashSet<>(Set.of(new Genre(6, "Боевик"))));
        genreRepository.updateGenresForFilm(savedFilm);

        // Проверка
        Set<Genre> updatedGenres = genreRepository.getGenresByFilmId(savedFilm.getId());
        assertThat(updatedGenres)
                .hasSize(1)
                .extracting(Genre::getName)
                .containsExactly("Боевик");
    }

    // Проверка корректности обновления жанров при удалении всех жанров
    @Test
    void shouldUpdateGenresForFilm_RemoveAllGenres() {
        film.setGenres(new LinkedHashSet<>(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма"))));
        Film savedFilm = filmRepository.createFilm(film);

        savedFilm.setGenres(new LinkedHashSet<>());
        genreRepository.updateGenresForFilm(savedFilm);

        Set<Genre> updatedGenres = genreRepository.getGenresByFilmId(savedFilm.getId());
        assertThat(updatedGenres).isEmpty();
    }

    @Test
    void shouldUpdateGenresForFilm_HandleNullGenreSet() {
        film.setGenres(new LinkedHashSet<>(Set.of(new Genre(1, "Комедия"))));
        Film savedFilm = filmRepository.createFilm(film);

        savedFilm.setGenres(null);
        genreRepository.updateGenresForFilm(savedFilm);

        Set<Genre> updatedGenres = genreRepository.getGenresByFilmId(savedFilm.getId());
        assertThat(updatedGenres).isEmpty();
    }
}
