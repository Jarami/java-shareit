package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.validator.NullOrNotEmpty;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @NullOrNotEmpty(message = "Название вещи не должно быть пустым")
    private String name;

    @NullOrNotEmpty(message = "Описание вещи не должно быть пустым")
    private String description;

    private Boolean available;
}
