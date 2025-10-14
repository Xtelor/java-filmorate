package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@Builder
@EqualsAndHashCode(of = {"id"})
public class Film {
    // Лайки фильма
    @Builder.Default
    private Set<Integer> likes = new LinkedHashSet<>();
    // Жанры фильма
    @Builder.Default
    private Set<Genre> genres = new LinkedHashSet<>();
    // Рейтинг MPA фильма
    @NotNull
    private Rating rating;

    // ID фильма
    @Builder.Default
    private int id = 0;

    // Название фильма
    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    // Описание фильма - не более 200 символов
    @NotBlank(message = "Описание фильма не может быть пустым.")
    @Size(max = 200, message = "Максимальная длина описания фильма — 200 символов.")
    private String description;

    // Дата релиза
    @NotNull(message = "Дата релиза не может быть пустой.")
    private LocalDate releaseDate;

    // Продолжительность фильма
    @NotNull(message = "Продолжительность фильма не может быть пустой.")
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Integer duration;

    // Проверка корректности даты релиза
    @AssertTrue(message = "Дата релиза — не раньше 28 декабря 1895 года.")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return true;
        }
        return releaseDate.isAfter(LocalDate.of(1895, 12, 28));
    }
}
