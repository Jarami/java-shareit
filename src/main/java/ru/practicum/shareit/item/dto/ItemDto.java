package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
}
