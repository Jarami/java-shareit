package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    private String start;

    private String end;

    private ItemDto item;

    private UserDto booker;

    private BookingStatus status;
}
