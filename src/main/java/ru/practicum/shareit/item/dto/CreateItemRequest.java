package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateItemRequest {

    @NotNull(message = "Название вещи должно быть задано")
    @NotBlank(message = "Название вещи не должно быть пустым")
    private String name;

    @NotNull(message = "Описание вещи должно быть задано")
    @NotBlank(message = "Описание вещи не должно быть пустым")
    private String description;

    @NotNull(message = "Доступность вещи должна быть задана")
    private Boolean available;
}
