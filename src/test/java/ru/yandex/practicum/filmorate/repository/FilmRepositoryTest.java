package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcRatingRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcUserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcFilmRepository.class, JdbcUserRepository.class, JdbcGenreRepository.class, JdbcRatingRepository.class,
        FilmRowMapper.class, UserRowMapper.class, GenreRowMapper.class, RatingRowMapper.class})
public class FilmRepositoryTest {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    private Film film;
    private Film anotherFilm;

    @BeforeEach
    void beforeEach() {
        Rating ratingG = new Rating(1, "G");
        Rating ratingPG = new Rating(2, "PG");

        Genre thriller = new Genre(4, "Триллер");
        Genre action = new Genre(6, "Боевик");

        film = Film.builder()
                .name("Чужой")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1979, 6, 22))
                .duration(116)
                .rating(ratingG)
                .genres(new HashSet<>(Set.of(thriller)))
                .build();

        anotherFilm = Film.builder()
                .name("Чужие")
                .description("Фантастика, Боевик")
                .releaseDate(LocalDate.of(1986, 7, 18))
                .duration(137)
                .rating(ratingPG)
                .genres(new HashSet<>(Set.of(action, thriller)))
                .build();
    }

    // Проверка возвращения пустого списка фильмов при пустой БД
    @Test
    void shouldGetEmptyFilmList() {
        Collection<Film> films = filmRepository.getAllFilms();
        assertThat(films).isEmpty();
    }

    // Проверка добавления корректного фильма
    @Test
    void shouldAddCorrectFilm() {
        Film savedFilm = filmRepository.createFilm(film);
        Optional<Film> foundFilm = filmRepository.getFilm(savedFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                   assertThat(f)
                           .hasFieldOrPropertyWithValue("id", savedFilm.getId())
                           .hasFieldOrPropertyWithValue("name", savedFilm.getName())
                           .hasFieldOrPropertyWithValue("description", savedFilm.getDescription())
                           .hasFieldOrPropertyWithValue("releaseDate", savedFilm.getReleaseDate())
                           .hasFieldOrPropertyWithValue("duration", savedFilm.getDuration())
                );

        assertThat(foundFilm.get().getRating()).hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
        assertThat(foundFilm.get().getGenres())
                .hasSize(1)
                .extracting(Genre::getName)
                .containsExactly("Триллер");
    }

    // Проверка добавления фильма с описанием, длина которого составляет 200 символов
    @Test
    void shouldAddFilmWithLongDescription() {
        String description = "OK".repeat(100);
        film.setDescription(description);

        Film savedFilm = filmRepository.createFilm(film);
        Optional<Film> foundFilm = filmRepository.getFilm(savedFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("description", description)
                );
    }

    // Проверка получения списка с одним фильмом
    @Test
    void shouldGetFilm() {
        Film savedFilm = filmRepository.createFilm(film);
        Collection<Film> films = filmRepository.getAllFilms();

        assertThat(films).hasSize(1);
        Film retrievedFilm = films.iterator().next();
        assertThat(retrievedFilm)
                .hasFieldOrPropertyWithValue("id", savedFilm.getId())
                .hasFieldOrPropertyWithValue("name", "Чужой");
    }

    // Проверка получения списка с несколькими фильмами
    @Test
    void shouldGetFilms() {
        Film savedFilm1 = filmRepository.createFilm(film);
        Film savedFilm2 = filmRepository.createFilm(anotherFilm);

        Collection<Film> films = filmRepository.getAllFilms();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Чужой", "Чужие");

        assertThat(films)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(savedFilm1.getId(), savedFilm2.getId());
    }

    // Проверка невозможности создания фильма без названия
    @Test
    void shouldNotCreateFilmWithoutName() {
        film.setName(null);
        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка возможности добавить фильм с пустым названием в БД
    @Test
    void shouldAddFilmWithBlankName() {
        film.setName("");
        Film savedFilm = filmRepository.createFilm(film);

        assertThat(savedFilm.getName()).isEqualTo("");
    }

    // Проверка невозможности создания фильма без описания
    @Test
    void shouldNotAddFilmWithoutDescription() {
        film.setDescription(null);

        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка возможности добавить фильм с пустым описанием в БД
    @Test
    void shouldAddFilmWithBlankDescription() {
        film.setDescription("");
        Film savedFilm = filmRepository.createFilm(film);

        assertThat(savedFilm.getDescription()).isEqualTo("");
    }

    // Проверка выбрасывания исключения при попытке добавить фильм со слишком длинным описанием
    @Test
    void shouldNotAddFilmWithTooLongDescription() {
        film.setDescription("OK".repeat(100) + ".");

        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка невозможности создания фильма без даты релиза
    @Test
    void shouldNotCreateFilmWithoutReleaseDate() {
        film.setReleaseDate(null);

        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка выбрасывания исключения при попытке добавить фильм с датой релиза раньше 28 декабря 1895 года
    @Test
    void shouldNotAddFilmWithIncorrectReleaseDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка выбрасывания исключения при попытке добавить фильм с нулевой длительностью
    @Test
    void shouldNotAddFilmWithZeroDuration() {
        film.setDuration(0);

        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка выбрасывания исключения при попытке добавить фильм с отрицательной длительностью
    @Test
    void shouldNotAddFilmWithNegativeDuration() {
        film.setDuration(-1);

        assertThatThrownBy(() -> filmRepository.createFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка корректности обновления фильма
    @Test
    void shouldUpdateFilm() {
        Film savedFilm = filmRepository.createFilm(film);
        Film filmToUpdate = Film.builder()
                .id(savedFilm.getId())
                .name("Чужой 2")
                .description("Фантастика, Ужасы")
                .releaseDate(LocalDate.of(1994, 3, 25))
                .duration(137)
                .rating(new Rating(2, "PG"))
                .genres(new HashSet<>(Set.of(new Genre(6, "Боевик"))))
                .build();

        filmRepository.updateFilm(filmToUpdate);
        Optional<Film> updatedFilm = filmRepository.getFilm(savedFilm.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Чужой 2")
                                .hasFieldOrPropertyWithValue("duration", 137)
                );

        assertThat(updatedFilm.get().getRating())
                .hasFieldOrPropertyWithValue("id", 2);

        assertThat(updatedFilm.get().getGenres())
                .hasSize(1)
                .extracting(Genre::getName)
                .containsExactly("Боевик");
    }

    // Проверка обновления фильма на фильм с описанием, длина которого составляет 200 символов
    @Test
    void shouldUpdateFilmWithLongDescription() {
        Film savedFilm = filmRepository.createFilm(film);
        String description = "OK".repeat(100);
        savedFilm.setDescription(description);
        filmRepository.updateFilm(savedFilm);
        Optional<Film> updatedFilmOpt = filmRepository.getFilm(savedFilm.getId());

        assertThat(updatedFilmOpt)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("description", description)
                );
    }

    // Проверка выбрасывания исключения при попытке обновить несуществующий фильм
    @Test
    void shouldNotUpdateNonExistentFilm() {
        filmRepository.createFilm(film);
        anotherFilm.setId(999);
        filmRepository.updateFilm(anotherFilm);

        Optional<Film> nonExistentFilm = filmRepository.getFilm(999);
        assertThat(nonExistentFilm).isEmpty();

        Collection<Film> films = filmRepository.getAllFilms();
        assertThat(films).hasSize(1);
    }

    // Проверка удаления фильма
    @Test
    void shouldDeleteFilm() {
        Film savedFilm = filmRepository.createFilm(film);
        assertThat(filmRepository.getFilm(savedFilm.getId())).isPresent();

        boolean isDeleted = filmRepository.deleteFilm(savedFilm.getId());

        assertThat(isDeleted).isTrue();
        assertThat(filmRepository.getFilm(savedFilm.getId())).isEmpty();
    }

    // Проверка возврата false при удалении несуществующего фильма
    @Test
    void shouldReturnFalseWhenDeletingNonExistentFilm() {
        boolean isDeleted = filmRepository.deleteFilm(999);
        assertThat(isDeleted).isFalse();
    }

    // Проверка удаления всех фильмов
    @Test
    void shouldDeleteAllFilms() {
        filmRepository.createFilm(film);
        filmRepository.createFilm(anotherFilm);
        assertThat(filmRepository.getAllFilms()).hasSize(2);

        filmRepository.deleteFilms();
        assertThat(filmRepository.getAllFilms()).isEmpty();
    }

    // Проверка добавления лайка
    @Test
    void shouldAddLike() {
        User user = userRepository.addUser(buildUser("user1", "user1@mail.com"));
        Film savedFilm = filmRepository.createFilm(film);
        filmRepository.addLike(savedFilm.getId(), user.getId());
        assertThat(filmRepository.getLikesCount(savedFilm.getId())).isEqualTo(1);
    }

    // Проверка удаления лайка
    @Test
    void shouldRemoveLike() {
        User user = userRepository.addUser(buildUser("user1", "user1@mail.com"));
        Film savedFilm = filmRepository.createFilm(film);
        filmRepository.addLike(savedFilm.getId(), user.getId());

        boolean isRemoved = filmRepository.removeLike(savedFilm.getId(), user.getId());
        assertThat(isRemoved).isTrue();
        assertThat(filmRepository.getLikesCount(savedFilm.getId())).isEqualTo(0);
    }

    // Проверка возвращения false при попытке удалить несуществующий лайк
    @Test
    void shouldReturnFalseWhenRemovingNonExistentLike() {
        User user = userRepository.addUser(buildUser("user1", "user1@mail.com"));
        Film savedFilm = filmRepository.createFilm(film);
        boolean isRemoved = filmRepository.removeLike(savedFilm.getId(), user.getId());

        assertThat(isRemoved).isFalse();
    }

    // Проверка выбрасывания исключения при попытке несуществующего пользователя поставить лайк
    @Test
    void shouldThrowExceptionWhenAddingLikeFromNonExistentUser() {
        Film savedFilm = filmRepository.createFilm(film);

        assertThatThrownBy(() -> filmRepository.addLike(savedFilm.getId(), 999))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Проверка корректности количества лайков фильма при их добавлении/удалении
    @Test
    void shouldGetLikesCount() {
        User user1 = userRepository.addUser(buildUser("user1", "u1@mail.com"));
        User user2 = userRepository.addUser(buildUser("user2", "u2@mail.com"));
        Film savedFilm = filmRepository.createFilm(film);

        assertThat(filmRepository.getLikesCount(savedFilm.getId())).isEqualTo(0);
        filmRepository.addLike(savedFilm.getId(), user1.getId());
        assertThat(filmRepository.getLikesCount(savedFilm.getId())).isEqualTo(1);
        filmRepository.addLike(savedFilm.getId(), user2.getId());
        assertThat(filmRepository.getLikesCount(savedFilm.getId())).isEqualTo(2);
        filmRepository.removeLike(savedFilm.getId(), user1.getId());
        assertThat(filmRepository.getLikesCount(savedFilm.getId())).isEqualTo(1);
    }

    // Проверка получения топа фильмов
    @Test
    void shouldGetMostPopularFilms() {
        User user1 = userRepository.addUser(buildUser("user1", "u1@m.com"));
        User user2 = userRepository.addUser(buildUser("user2", "u2@m.com"));
        User user3 = userRepository.addUser(buildUser("user3", "u3@m.com"));
        Genre thriller = new Genre(4, "Триллер");

        Film film1 = filmRepository.createFilm(film);
        Film film2 = filmRepository.createFilm(anotherFilm);
        Film film3 = Film.builder()
                .name("Прометей")
                .description("Фантастика")
                .releaseDate(LocalDate.of(2012, 5, 30))
                .duration(124)
                .rating(new Rating(3, "PG-13"))
                .genres(new HashSet<>(Set.of(thriller)))
                .build();
        film3 = filmRepository.createFilm(film3);

        filmRepository.addLike(film2.getId(), user1.getId());
        filmRepository.addLike(film2.getId(), user2.getId());
        filmRepository.addLike(film2.getId(), user3.getId());
        filmRepository.addLike(film1.getId(), user1.getId());
        filmRepository.addLike(film1.getId(), user2.getId());
        filmRepository.addLike(film3.getId(), user3.getId());

        List<Film> popularFilms = filmRepository.getMostPopularFilms(3);
        assertThat(popularFilms)
                .hasSize(3)
                .extracting(Film::getName)
                .containsExactly("Чужие", "Чужой", "Прометей");
    }

    // Проверка получения пустого топа при пустой БД
    @Test
    void shouldReturnEmptyListForMostPopularWhenDbIsEmpty() {
        List<Film> popularFilms = filmRepository.getMostPopularFilms(10);
        assertThat(popularFilms).isEmpty();
    }

    // Метод для создания объекта User для тестов
    private User buildUser(String login, String email) {
        return User.builder()
                .login(login)
                .email(email)
                .name(login)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }
}
