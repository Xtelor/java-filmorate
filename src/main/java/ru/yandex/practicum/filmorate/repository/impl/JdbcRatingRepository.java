package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.RatingRepository;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRatingRepository extends BaseRepository<Rating> implements RatingRepository {
    // 1. ПОЛУЧЕНИЕ ВСЕХ РЕЙТИНГОВ
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_rating ORDER BY id";
    // 2. ПОЛУЧЕНИЕ РЕЙТИНГА ПО ID
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_rating WHERE id = :id";

    public JdbcRatingRepository(NamedParameterJdbcOperations jdbc) {
        super(jdbc, (rs, rowNum) -> new Rating(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }

    @Override
    public List<Rating> findAll() {
        return findAll(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Rating> findById(int id) {
        return findOne(FIND_BY_ID_QUERY,
                new MapSqlParameterSource("id", id));
    }
}
