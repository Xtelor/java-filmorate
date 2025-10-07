package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.repository.RatingRepository;
import ru.yandex.practicum.filmorate.service.RatingService;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;

    // Получение списка всех рейтингов
    @Override
    public List<Rating> getAll() {
        log.info("Запрос на получение списка всех рейтингов.");
        return ratingRepository.findAll();
    }

    // Получение рейтинга по его ID
    @Override
    public Rating getById(int id) {
        log.info("Запрос на получение рейтинга MPA с id = {}.", id);
        return ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден."));
    }
}
