package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class JdbcFilmRepository extends BaseRepository<Film> implements FilmRepository {
    private final GenreRepository genreRepository;

    private static final String SELECT_FILM_WITH_MPA =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "f.rating_id, mr.name AS mpa_name " +
                    "FROM films AS f " +
                    "JOIN mpa_rating AS mr ON f.rating_id = mr.id ";

    // 1. ПОЛУЧЕНИЕ ФИЛЬМОВ
    private static final String FIND_ALL_QUERY = SELECT_FILM_WITH_MPA;
    private static final String FIND_BY_ID_QUERY = SELECT_FILM_WITH_MPA + "WHERE f.id = :id";

    // 2. СОЗДАНИЕ ФИЛЬМА
    private static final String INSERT_QUERY =
            "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                    "VALUES (:name, :description, :releaseDate, :duration, :ratingId)";

    // 3. ОБНОВЛЕНИЕ ФИЛЬМА
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = :name, description = :description, " +
                    "release_date = :releaseDate, duration = :duration, rating_id = :ratingId " +
                    "WHERE id = :id";

    // 4. УДАЛЕНИЕ ФИЛЬМОВ
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM films WHERE id = :id";
    private static final String DELETE_ALL_QUERY = "DELETE FROM films";

    // 5. ДОБАВЛЕНИЕ ЛАЙКА
    private static final String ADD_LIKE_QUERY =
            "INSERT INTO likes (film_id, user_id) VALUES (:filmId, :userId)";

    // 6. УДАЛЕНИЕ ЛАЙКА
    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM likes WHERE film_id = :filmId AND user_id = :userId";

    // 7. ПОЛУЧЕНИЕ САМЫХ ПОПУЛЯРНЫХ ФИЛЬМОВ
    private static final String GET_MOST_POPULAR_FILMS_QUERY =
            SELECT_FILM_WITH_MPA +
                    "LEFT JOIN likes AS l ON f.id = l.film_id " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(DISTINCT l.user_id) DESC " +
                    "LIMIT :count";

    // 8. ЗАГРУЗКА ЖАНРОВ
    private static final String GET_GENRES_FOR_FILMS_QUERY =
            "SELECT g.*, fg.film_id " +
                    "FROM genre AS g JOIN film_genre AS fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id IN (:filmIds) " +
                    "ORDER BY fg.film_id, g.id";

    // 9. ПОЛУЧЕНИЕ КОЛИЧЕСТВА ЛАЙКОВ
    private static final String GET_LIKES_COUNT_QUERY = "SELECT COUNT(user_id) " +
            "FROM likes WHERE film_id = :filmId";

    @Autowired
    public JdbcFilmRepository(NamedParameterJdbcOperations jdbc,
                              FilmRowMapper mapper, GenreRepository genreRepository) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
    }

    // Получение списка всех фильмов
    @Override
    public List<Film> getAllFilms() {
        List<Film> films = findAll(FIND_ALL_QUERY);
        loadGenresForFilms(films);
        return films;
    }

    // Получение фильма по id
    @Override
    public Optional<Film> getFilm(int id) {
        Optional<Film> filmOptional = findOne(FIND_BY_ID_QUERY, new MapSqlParameterSource("id", id));
        filmOptional.ifPresent(this::loadGenresForFilm);
        return filmOptional;
    }

    // Удаление фильма по id: true - если удалили
    @Override
    public boolean deleteFilm(int id) {
        return update(DELETE_BY_ID_QUERY,
                new MapSqlParameterSource("id", id)) > 0;
    }

    // Удаление всех фильмов
    @Override
    public void deleteFilms() {
        update(DELETE_ALL_QUERY);
    }

    // Добавление фильма (возвращает объект с установленным id)
    @Override
    public Film createFilm(Film film) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("ratingId", film.getRating().getId());

        int id = insert(INSERT_QUERY, params);
        film.setId(id);
        genreRepository.updateGenresForFilm(film);

        return film;
    }

    // Обновление фильма (true — если обновлена хотя бы одна строка)
    @Override
    public boolean updateFilm(Film film) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("id", film.getId())
                .addValue("ratingId", film.getRating().getId());

        boolean isUpdated = update(UPDATE_QUERY, params) > 0;

        if (isUpdated) {
            genreRepository.updateGenresForFilm(film);
        }
        return isUpdated;
    }

    // Добавление лайка
    @Override
    public void addLike(int filmId, int userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        update(ADD_LIKE_QUERY, params);
    }

    // Удаление лайка
    @Override
    public boolean removeLike(int filmId, int userId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        return update(REMOVE_LIKE_QUERY, params) > 0;
    }

    // Получение количества лайков фильма
    @Override
    public int getLikesCount(int id) {
        SqlParameterSource params = new MapSqlParameterSource("filmId", id);
        return jdbc.queryForObject(GET_LIKES_COUNT_QUERY, params, Integer.class);
    }

    // Получение топа популярных фильмов
    @Override
    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = findAll(GET_MOST_POPULAR_FILMS_QUERY,
                new MapSqlParameterSource("count", count));
        loadGenresForFilms(films);
        return films;
    }

    // Метод для загрузки жанров фильма
    private void loadGenresForFilm(Film film) {
        film.setGenres(genreRepository.getGenresByFilmId(film.getId()));
    }

    // Метод для загрузки жанров списка фильма
    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        // Список ID всех фильмов
        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        // Все жанры для всех этих фильмов
        SqlParameterSource params = new MapSqlParameterSource("filmIds", filmIds);

        final Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();

        jdbc.query(GET_GENRES_FOR_FILMS_QUERY, params, (rs) -> {
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            int filmId = rs.getInt("film_id");
            genresByFilmId.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });

        // Распределение жанров по соответствующим фильмам
        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptySet()));
        }
    }
}
