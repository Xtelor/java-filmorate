package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Базовый репозиторий
@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final NamedParameterJdbcOperations jdbc;
    protected final RowMapper<T> mapper;

    // Нахождение конкретного объекта
    protected Optional<T> findOne(String query, SqlParameterSource params) {
        try {
            T result = jdbc.queryForObject(query, params, mapper);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    // Нахождение всех объектов
    protected List<T> findAll(String query, SqlParameterSource params) {
        return jdbc.query(query, params, mapper);
    }

    // Нахождение всех объектов для запросов без параметров
    protected List<T> findAll(String query) {
        return jdbc.query(query, mapper);
    }

    // Универсальный метод для обновления данных(UPDATE, DELETE)
    // Возвращает количество измененных строк
    protected int update(String query, SqlParameterSource params) {
       return jdbc.update(query, params);
    }

    // Обновление данных для запросов без параметров
    protected void update(String query) {
        jdbc.update(query, Map.of());
    }

    // Вставка нового объекта
    protected int insert(String query,  SqlParameterSource params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(query, params, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        // Возвращаем id нового объекта
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }
}