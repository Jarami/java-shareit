package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;
import ru.practicum.shareit.validator.NullOrNotEmpty;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NullOrNotEmpty(message = "Имя пользователя не должно быть пустым")
    private String name;

    @Email
    private String email;
}
