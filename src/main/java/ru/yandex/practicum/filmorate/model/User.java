package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */
@Data
@Builder
@EqualsAndHashCode(of = {"id"})
public class User {
    // Список друзей пользователя
    private Set<Integer> friends;

    // ID пользователя
    @Builder.Default
    private int id = 0;

    // Электронная почта пользователя
    @NotBlank(message = "Электронная почта не может быть пустой.")
    @Email(message = "Электронная почта должна содержать символ @.")
    private String email;

    // Логин пользователя
    @NotBlank(message = "Логин не может быть пустым.")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы.")
    private String login;

    // Имя пользователя
    private String name;

    // День рождения пользователя
    @NotNull(message = "Дата рождения не может быть пустой.")
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;

    // Получение списка ID друзей пользователя
    public Set<Integer> getFriends() {
        if (this.friends == null) {
            this.friends = new HashSet<>();
        }

        return this.friends;
    }
}
