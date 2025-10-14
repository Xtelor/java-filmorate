package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcGenreRepository extends BaseRepository<Genre> implements GenreRepository {
    // 1. ПОЛУЧЕНИЕ ВСЕХ ЖАНРОВ
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY id";
    // 2. ПОИСК ЖАНРА ПО ID
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = :id";
    // 3. ПОЛУЧЕНИЕ ЖАНРОВ ФИЛЬМА
    private static final String GET_GENRES_BY_FILM_ID_QUERY =
            "SELECT g.* FROM genre AS g JOIN film_genre AS fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = :filmId " +
                    "ORDER BY g.id";
    // 4. УДАЛЕНИЕ ВСЕХ ЖАНРОВ ИЗ ФИЛЬМА
    private static final String REMOVE_ALL_GENRES_FROM_FILM_QUERY = "DELETE FROM film_genre " +
            "WHERE film_id = :filmId";
    // 5. ДОБАВЛЕНИЕ ЖАНРА В ФИЛЬМ
    private static final String ADD_GENRE_TO_FILM_QUERY =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (:filmId, :genreId)";

    public JdbcGenreRepository(NamedParameterJdbcOperations jdbc) {
        super(jdbc, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public List<Genre> findAll() {
        return findAll(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY,
                new MapSqlParameterSource("id", id));
    }

    @Override
    public Set<Genre> getGenresByFilmId(int filmId) {
        List<Genre> genres = findAll(GET_GENRES_BY_FILM_ID_QUERY,
                new MapSqlParameterSource("filmId", filmId));
        return new LinkedHashSet<>(genres);
    }

    @Override
    public void updateGenresForFilm(Film film) {
        // 1. Удаляем все текущие жанры фильма, чтобы избежать дубликатов и убрать лишние
        jdbc.update(REMOVE_ALL_GENRES_FROM_FILM_QUERY,
                new MapSqlParameterSource("filmId", film.getId()));

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return; // Если новых жанров нет, просто выходим
        }

        // 2. Готовим параметры для пакетной вставки новых жанров
        SqlParameterSource[] batchParams = film.getGenres().stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("filmId", film.getId())
                        .addValue("genreId", genre.getId()))
                .toArray(SqlParameterSource[]::new);

        // 3. Выполняем пакетную вставку всех жанров фильма одним запросом
        if (batchParams.length > 0) {
            jdbc.batchUpdate(ADD_GENRE_TO_FILM_QUERY, batchParams);
        }
    }
}
