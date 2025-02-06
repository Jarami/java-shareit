package ru.practicum.shareit.item.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private List<CommentDto> comments;
    private String lastBooking;
    private String nextBooking;
}
