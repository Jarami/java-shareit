package ru.practicum.shareit.itemRequest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateItemRequestRequest {

    @NotBlank(message = "Текст запроса не должен быт пустым")
    private String description;
}
