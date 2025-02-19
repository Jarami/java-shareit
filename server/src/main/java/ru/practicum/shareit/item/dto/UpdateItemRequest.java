package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.validator.NullOrNotEmpty;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @NullOrNotEmpty(message = "Название вещи не должно быть пустым")
    private String name;

    @NullOrNotEmpty(message = "Описание вещи не должно быть пустым")
    private String description;

    private Boolean available;
}
