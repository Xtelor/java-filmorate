package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class FilmDto {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    private int likesCount;
    @JsonProperty("mpa")
    private Rating rating;
    private Set<Genre> genres;
}
