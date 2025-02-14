package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemMapperImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserMapperImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingMapperTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private BookingMapper mapper;

    @BeforeEach
    void setup() {
        UserMapper userMapper = new UserMapperImpl();
        ItemMapper itemMapper = new ItemMapperImpl();
        mapper = new BookingMapperImpl(itemMapper, userMapper);
    }

    @Test
    void givenNull_whenToDto_gotNull() {
        Booking booking = null;
        BookingDto dto = mapper.toDto(booking);
        assertNull(dto);
    }

    @Test
    void givenNullBookings_whenToDto_gotNull() {
        List<Booking> bookings = null;
        List<BookingDto> dtos = mapper.toDto(bookings);

        assertNull(dtos);
    }

    @Test
    void givenNoStartAndEnd_whenToDto_gotNoStartAndEnd() {
        User booker = new User(2L, "booker", "booker@mail.ru");
        User owner = createUser();
        Item item = createItem(owner);
        Booking booking = new Booking(1L, null, null, item, booker, BookingStatus.WAITING);

        BookingDto dto = mapper.toDto(booking);
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
    }

    @Test
    void givenValidBooking_whenToDto_gotValidDto() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        User owner = new User(1L, "owner", "owner@mail.ru");
        User booker = new User(2L, "booker", "booker@mail.ru");

        Item item = createItem(owner);
        item.getComments().add(new Comment(5L, "comment 1", booker, item, Instant.now()));
        item.getComments().add(new Comment(6L, "comment 2", booker, item, Instant.now()));

        Booking booking = new Booking(3L, start, end, item, booker, BookingStatus.WAITING);

        BookingDto dto = mapper.toDto(booking);

        assertEquals(3L, dto.getId());
        assertEquals(formatter.format(start), dto.getStart());
        assertEquals(formatter.format(end), dto.getEnd());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(5L, dto.getItem().getComments().get(0).getId());
        assertEquals(6L, dto.getItem().getComments().get(1).getId());
        assertEquals(booker.getId(), dto.getBooker().getId());
    }

    @Test
    void givenNullRequest_whenToBooking_gotNullBooking() {
        CreateBookingRequest request = null;
        Booking booking = mapper.toBooking(request);
        assertNull(booking);
    }

    @Test
    void givenValidRequest_whenToBooking_gotValidBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        CreateBookingRequest request = new CreateBookingRequest(1L, start, end);
        Booking booking = mapper.toBooking(request);

        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
    }

    private User createUser() {
        return new User(1L, "user", "user@mail.ru");
    }

    private Item createItem(User owner) {
        return new Item(1L, owner, "item", "item desc", true, new ArrayList<>());
    }
}