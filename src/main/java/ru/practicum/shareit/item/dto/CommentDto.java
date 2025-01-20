package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CommentDto {
    private Long id;
    private String text;
    private String authorName;
    private Instant created;
}
