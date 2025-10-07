package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {
    // Название фильма
    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    // Описание фильма
    @NotBlank(message = "Описание фильма не может быть пустым.")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    // Дата релиза фильма
    @NotNull(message = "Дата релиза не может быть пустой.")
    private LocalDate releaseDate;

    // Продолжительность фильма
    @NotNull(message = "Продолжительность фильма не может быть пустой.")
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Integer duration;

    // Рейтинг фильма
    @JsonProperty("mpa")
    @NotNull(message = "Рейтинг не может быть пустым.")
    private Rating rating;

    // Жанры фильма
    private Set<Genre> genres;

    // Метод валидации даты: true - если дата корректна
    @JsonIgnore
    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года.")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return true;
        }
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
