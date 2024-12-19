package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotNull(message = "Имя пользователя должно быть задано")
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String name;

    @NotNull(message = "Почта пользователя должно быть задана")
    @Email
    private String email;
}
