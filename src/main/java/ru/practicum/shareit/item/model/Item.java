package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@ToString
@Builder
public class Item {
    private Long id;
    private User owner;
    private String name;
    private String description;
    private boolean available;
    private int bookCount;
}
