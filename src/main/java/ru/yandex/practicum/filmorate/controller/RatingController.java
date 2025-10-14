package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@Validated
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @GetMapping
    public List<Rating> getAllRatings() {
        log.info("Выполнение метода getAllRatings.");
        return ratingService.getAll();
    }

    @GetMapping("/{id}")
    public Rating getRatingById(@PathVariable @Positive int id) {
        log.info("Выполнение метода getRatingById.");
        return ratingService.getById(id);
    }
}
