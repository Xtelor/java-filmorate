package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    @Builder.Default
    private int id = 0;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    @NotBlank(message = "Описание фильма не может быть пустым.")
    @Size(max = 200, message = "Максимальная длина описания фильма — 200 символов.")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой.")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма не может быть пустой.")
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Integer duration;

    @AssertTrue(message = "Дата релиза — не раньше 28 декабря 1895 года.")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return true;
        }
        return releaseDate.isAfter(LocalDate.of(1895, 12, 28));
    }
}
