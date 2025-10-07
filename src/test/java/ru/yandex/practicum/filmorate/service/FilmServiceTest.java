package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.impl.FilmServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilmServiceTest {
    @Mock
    private FilmRepository filmRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RatingService ratingService;
    @Mock
    private GenreService genreService;

    @InjectMocks
    private FilmServiceImpl filmService;

    private Film film;
    private NewFilmRequest newFilmRequest;
    private UpdateFilmRequest updateFilmRequest;
    private User user;
    private Rating rating;
    private Genre genre;

    @BeforeEach
    void beforeEach() {
        rating = new Rating(1, "G");
        genre = new Genre(1, "Комедия");
        user = User.builder().id(1).login("testUser").build();

        film = Film.builder()
                .id(1)
                .name("Тестовый фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .rating(rating)
                .genres(Set.of(genre))
                .build();

        newFilmRequest = new NewFilmRequest();
        newFilmRequest.setName("Тестовый фильм");
        newFilmRequest.setDescription("Описание");
        newFilmRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilmRequest.setDuration(120);
        newFilmRequest.setRating(rating);
        newFilmRequest.setGenres(Set.of(genre));

        updateFilmRequest = new UpdateFilmRequest();
        updateFilmRequest.setId(1);
        updateFilmRequest.setName("Обновленный фильм");
        updateFilmRequest.setDescription("Новое описание");
        updateFilmRequest.setReleaseDate(LocalDate.of(2001, 2, 2));
        updateFilmRequest.setDuration(130);
        updateFilmRequest.setRating(rating);
        updateFilmRequest.setGenres(Set.of(genre));
    }

    // Проверка успешного создания фильма
    @Test
    void shouldCreateFilmSuccessfully() {
        when(filmRepository.createFilm(any(Film.class))).thenReturn(film);
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        FilmDto result = filmService.create(newFilmRequest);

        assertNotNull(result);
        assertEquals(film.getId(), result.getId());
        assertEquals(newFilmRequest.getName(), result.getName());
        verify(filmRepository, times(1)).createFilm(any(Film.class));
    }

    // Проверка успешного создания фильма с описанием длиной 200 символов
    @Test
    void shouldCreateFilmSuccessfullyWithMaxDescriptionLength() {
        String maxLenDescription = "a".repeat(200);
        newFilmRequest.setDescription(maxLenDescription);

        Film filmWithMaxDesc = Film.builder()
                .id(1)
                .name(newFilmRequest.getName())
                .description(maxLenDescription)
                .releaseDate(newFilmRequest.getReleaseDate())
                .duration(newFilmRequest.getDuration())
                .rating(newFilmRequest.getRating())
                .genres(newFilmRequest.getGenres())
                .build();

        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);
        when(filmRepository.createFilm(any(Film.class))).thenReturn(filmWithMaxDesc);

        FilmDto result = filmService.create(newFilmRequest);

        assertNotNull(result);
        assertEquals(maxLenDescription, result.getDescription());
        verify(filmRepository, times(1)).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм с несуществующим рейтингом
    @Test
    void shouldThrowNotFoundExceptionWhenCreatingFilmWithNonExistentRating() {
        doThrow(new NotFoundException("Рейтинг не найден")).when(ratingService).getById(anyInt());

        assertThrows(NotFoundException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм с несуществующим жанром
    @Test
    void shouldThrowNotFoundExceptionWhenCreatingFilmWithNonExistentGenre() {
        when(ratingService.getById(anyInt())).thenReturn(rating);
        doThrow(new NotFoundException("Жанр не найден")).when(genreService).getById(anyInt());

        assertThrows(NotFoundException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм без названия
    @Test
    void shouldThrowValidationExceptionWhenCreatingFilmWithBlankName() {
        newFilmRequest.setName(" ");
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм со слишком длинным описанием
    @Test
    void shouldThrowValidationExceptionWhenCreatingFilmWithLongDescription() {
        newFilmRequest.setDescription("a".repeat(201));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм с недопустимой датой релиза
    @Test
    void shouldThrowValidationExceptionWhenCreatingFilmWithEarlyReleaseDate() {
        newFilmRequest.setReleaseDate(LocalDate.of(1895, 12, 27));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм с отрицательной длительностью
    @Test
    void shouldThrowValidationExceptionWhenCreatingFilmWithNegativeDuration() {
        newFilmRequest.setDuration(-100);
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке создать фильм с нулевой длительностью
    @Test
    void shouldThrowValidationExceptionWhenCreatingFilmWithZeroDuration() {
        newFilmRequest.setDuration(0);
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.create(newFilmRequest));
        verify(filmRepository, never()).createFilm(any(Film.class));
    }

    // Проверка успешного обновления фильма
    @Test
    void shouldUpdateFilmSuccessfully() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(filmRepository.updateFilm(any(Film.class))).thenReturn(true);
        when(filmRepository.getLikesCount(1)).thenReturn(0);
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        FilmDto result = filmService.update(updateFilmRequest);

        assertNotNull(result);
        assertEquals(updateFilmRequest.getId(), result.getId());
        assertEquals(updateFilmRequest.getName(), result.getName());
        verify(filmRepository, times(1)).getFilm(1);
        verify(filmRepository, times(1)).updateFilm(any(Film.class));
        verify(filmRepository, times(1)).getLikesCount(1);
    }

    // Проверка успешного обновления фильма с описанием длиной 200 символов
    @Test
    void shouldUpdateFilmSuccessfullyWithMaxDescriptionLength() {
        String maxLenDescription = "a".repeat(200);
        updateFilmRequest.setDescription(maxLenDescription);

        when(filmRepository.getFilm(anyInt())).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);
        when(filmRepository.updateFilm(any(Film.class))).thenReturn(true);
        when(filmRepository.getLikesCount(anyInt())).thenReturn(0);

        FilmDto result = filmService.update(updateFilmRequest);

        assertNotNull(result);
        assertEquals(maxLenDescription, result.getDescription());
        verify(filmRepository, times(1)).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить несуществующий фильм
    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentFilm() {
        when(filmRepository.getFilm(99)).thenReturn(Optional.empty());

        updateFilmRequest.setId(99);

        assertThrows(NotFoundException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить рейтинг несуществующим
    @Test
    void shouldThrowNotFoundExceptionWhenRatingNotFoundDuringUpdate() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        doThrow(new NotFoundException("Рейтинг не найден")).when(ratingService).getById(anyInt());

        assertThrows(NotFoundException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить жанр несуществующим
    @Test
    void shouldThrowNotFoundExceptionWhenGenreNotFoundDuringUpdate() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        doThrow(new NotFoundException("Жанр не найден")).when(genreService).getById(anyInt());

        assertThrows(NotFoundException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить название фильма на пустое
    @Test
    void shouldThrowValidationExceptionWhenUpdatingFilmWithBlankName() {
        updateFilmRequest.setName("");
        when(filmRepository.getFilm(anyInt())).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить описание фильма на слишком длинное
    @Test
    void shouldThrowValidationExceptionWhenUpdatingFilmWithLongDescription() {
        updateFilmRequest.setDescription("a".repeat(201));
        when(filmRepository.getFilm(anyInt())).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить дату релиза фильма на недопустимую
    @Test
    void shouldThrowValidationExceptionWhenUpdatingFilmWithEarlyReleaseDate() {
        updateFilmRequest.setReleaseDate(LocalDate.of(1800, 1, 1)); // Некорректное поле
        when(filmRepository.getFilm(anyInt())).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить длительность фильма на отрицательную
    @Test
    void shouldThrowValidationExceptionWhenUpdatingFilmWithNegativeDuration() {
        updateFilmRequest.setDuration(-100);
        when(filmRepository.getFilm(anyInt())).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка выбрасывания исключения при попытке обновить длительность фильма на нулевую
    @Test
    void shouldThrowValidationExceptionWhenUpdatingFilmWithZeroDuration() {
        updateFilmRequest.setDuration(0);
        when(filmRepository.getFilm(anyInt())).thenReturn(Optional.of(film));
        when(ratingService.getById(anyInt())).thenReturn(rating);
        when(genreService.getById(anyInt())).thenReturn(genre);

        assertThrows(ValidationException.class, () -> filmService.update(updateFilmRequest));
        verify(filmRepository, never()).updateFilm(any(Film.class));
    }

    // Проверка получения фильма по ID
    @Test
    void shouldGetFilmById() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(filmRepository.getLikesCount(1)).thenReturn(5);

        FilmDto result = filmService.get(1);

        assertNotNull(result);
        assertEquals(film.getName(), result.getName());
        assertEquals(5, result.getLikesCount());
        verify(filmRepository, times(1)).getFilm(1);
        verify(filmRepository, times(1)).getLikesCount(1);
    }

    // Проверка выбрасывания исключения при попытке получить по ID несуществующий фильм
    @Test
    void shouldThrowNotFoundExceptionWhenGettingNonExistentFilm() {
        when(filmRepository.getFilm(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.get(99));
    }

    // Проверка получения списка всех фильмов
    @Test
    void shouldGetAllFilms() {
        Film film2 = Film.builder().id(2).name("Фильм 2").build();
        when(filmRepository.getAllFilms()).thenReturn(List.of(film, film2));
        when(filmRepository.getLikesCount(1)).thenReturn(3);
        when(filmRepository.getLikesCount(2)).thenReturn(10);

        List<FilmDto> result = filmService.getAll();

        assertEquals(2, result.size());
        assertEquals(3, result.get(0).getLikesCount());
        assertEquals(10, result.get(1).getLikesCount());
        verify(filmRepository, times(1)).getAllFilms();
        verify(filmRepository, times(2)).getLikesCount(anyInt());
    }

    // Проверка получения пустого списка
    @Test
    void shouldGetEmptyList() {
        when(filmRepository.getAllFilms()).thenReturn(Collections.emptyList());

        List<FilmDto> result = filmService.getAll();

        assertTrue(result.isEmpty());
    }

    // Проверка удаления фильма по ID
    @Test
    void shouldDeleteFilmById() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(filmRepository.deleteFilm(1)).thenReturn(true);

        filmService.delete(1);

        verify(filmRepository, times(1)).getFilm(1);
        verify(filmRepository, times(1)).deleteFilm(1);
    }

    // Проверка выбрасывания исключения при попытке удалить несуществующий фильм
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentFilm() {
        when(filmRepository.getFilm(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.delete(99));
        verify(filmRepository, never()).deleteFilm(99);
    }

    // Проверка удаления всех фильмов
    @Test
    void shouldDeleteAllFilms() {
        doNothing().when(filmRepository).deleteFilms();

        filmService.deleteAll();

        verify(filmRepository, times(1)).deleteFilms();
    }

    // Проверка успешного добавления лайка
    @Test
    void shouldAddLikeSuccessfully() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(userRepository.getUser(1)).thenReturn(Optional.of(user));
        doNothing().when(filmRepository).addLike(1, 1);

        filmService.addLike(1, 1);
        verify(filmRepository, times(1)).addLike(1, 1);
    }

    // Проверка выбрасывания исключения при попытке добавить лайк несуществующему фильму
    @Test
    void shouldThrowNotFoundExceptionWhenAddingLikeToNonExistentFilm() {
        when(filmRepository.getFilm(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(99, 1));
        verify(filmRepository, never()).addLike(anyInt(), anyInt());
    }

    // Проверка выбрасывания исключения при попытке добавить лайк от несуществующего пользователя
    @Test
    void shouldThrowNotFoundExceptionWhenAddingLikeFromNonExistentUser() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(userRepository.getUser(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.addLike(1, 99));
        verify(filmRepository, never()).addLike(anyInt(), anyInt());
    }

    // Проверка успешного удаления лайка
    @Test
    void shouldDeleteLikeSuccessfully() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(userRepository.getUser(1)).thenReturn(Optional.of(user));
        when(filmRepository.removeLike(1, 1)).thenReturn(true);

        filmService.deleteLike(1, 1);

        verify(filmRepository, times(1)).removeLike(1, 1);
    }

    // Проверка выбрасывания исключения при попытке удалить лайк с несуществующего фильма
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingLikeFromNonExistentFilm() {
        when(filmRepository.getFilm(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> filmService.deleteLike(99, 1));
        verify(userRepository, never()).getUser(anyInt());
        verify(filmRepository, never()).removeLike(anyInt(), anyInt());
    }

    // Проверка выбрасывания исключения при попытке удалить лайк несуществующего пользователя
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingLikeFromNonExistentUser() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(userRepository.getUser(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(1, 99));
        verify(filmRepository, never()).removeLike(anyInt(), anyInt());
    }

    // Проверка выбрасывания исключения при попытке удалить несуществующий лайк
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentLike() {
        when(filmRepository.getFilm(1)).thenReturn(Optional.of(film));
        when(userRepository.getUser(1)).thenReturn(Optional.of(user));
        when(filmRepository.removeLike(1, 1)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(1, 1));
    }

    // Проверка успешного получения топа фильмов
    @Test
    void shouldGetTop() {
        when(filmRepository.getMostPopularFilms(1)).thenReturn(List.of(film));
        when(filmRepository.getLikesCount(1)).thenReturn(10);

        List<FilmDto> result = filmService.getTop(1);

        assertEquals(1, result.size());
        assertEquals(10, result.getFirst().getLikesCount());
    }

    // Проверка получения пустого топа
    @Test
    void getTop_whenNoFilms_shouldReturnEmptyList() {
        when(filmRepository.getMostPopularFilms(10)).thenReturn(Collections.emptyList());
        List<FilmDto> result = filmService.getTop(10);
        assertTrue(result.isEmpty());
    }
}
