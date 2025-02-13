package ru.practicum.shareit.booking;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {

    @Mapping(target = "start", source = "start", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "end", source = "end", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    BookingDto toDto(Booking booking);

    List<BookingDto> toDto(List<Booking> bookings);

    Booking toBooking(CreateBookingRequest request);
}
