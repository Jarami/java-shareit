package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({BookingMapperImpl.class})
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private BookingMapper bookingMapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    LocalDateTime start;
    LocalDateTime end;
    User owner;
    User booker;
    Item item;
    Booking booking;

    @BeforeEach
    void setup() {
        start = LocalDateTime.parse("2025-01-01T00:00:00");
        end = LocalDateTime.parse("2025-01-02T00:00:00");
        owner = new User(1L, "owner", "owner@mail.ru");
        booker = new User(2L, "booker", "booker@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, new ArrayList<>());
        booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);
    }

    @Test
    void createBooking() throws Exception {

        Mockito
                .when(bookingService.createBooking(Mockito.any(CreateBookingRequest.class), Mockito.any(Long.class)))
                .thenReturn(booking);

        CreateBookingRequest request = new CreateBookingRequest(item.getId(), start, end);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.start", equalTo("2025-01-01T00:00:00")))
                .andExpect(jsonPath("$.end", equalTo("2025-01-02T00:00:00")))
                .andExpect(jsonPath("$.item.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.item.name", equalTo("item")))
                .andExpect(jsonPath("$.item.description", equalTo("some item")))
                .andExpect(jsonPath("$.item.available", equalTo(true)))
                .andExpect(jsonPath("$.item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$.booker.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$.booker.name", equalTo("booker")))
                .andExpect(jsonPath("$.booker.email", equalTo("booker@mail.ru")))
                .andExpect(jsonPath("$.status", equalTo("WAITING")));
    }

    @Test
    void approveBooking() throws Exception {

        Mockito
                .when(bookingService.approveBooking(booking.getId(), true, owner.getId()))
                .thenReturn(booking);

        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.start", equalTo("2025-01-01T00:00:00")))
                .andExpect(jsonPath("$.end", equalTo("2025-01-02T00:00:00")))
                .andExpect(jsonPath("$.item.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.item.name", equalTo("item")))
                .andExpect(jsonPath("$.item.description", equalTo("some item")))
                .andExpect(jsonPath("$.item.available", equalTo(true)))
                .andExpect(jsonPath("$.item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$.booker.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$.booker.name", equalTo("booker")))
                .andExpect(jsonPath("$.booker.email", equalTo("booker@mail.ru")))
                .andExpect(jsonPath("$.status", equalTo("WAITING")));
    }

    @Test
    void getBookingInfo() throws Exception {

        Mockito
                .when(bookingService.getBookingByIdAndUser(booking.getId(), owner.getId()))
                .thenReturn(booking);

        mvc.perform(get("/bookings/" + booking.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.start", equalTo("2025-01-01T00:00:00")))
                .andExpect(jsonPath("$.end", equalTo("2025-01-02T00:00:00")))
                .andExpect(jsonPath("$.item.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.item.name", equalTo("item")))
                .andExpect(jsonPath("$.item.description", equalTo("some item")))
                .andExpect(jsonPath("$.item.available", equalTo(true)))
                .andExpect(jsonPath("$.item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$.booker.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$.booker.name", equalTo("booker")))
                .andExpect(jsonPath("$.booker.email", equalTo("booker@mail.ru")))
                .andExpect(jsonPath("$.status", equalTo("WAITING")));
    }

    @Test
    void getCurrentUserBookings() throws Exception {

        LocalDateTime now = LocalDateTime.parse("2025-01-01T00:00:00");

        Mockito
                .when(bookingService.getCurrentUserBookings("WAITING", owner.getId(), now))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings?state=WAITING&now=2025-01-01T00:00:00")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$[0].start", equalTo("2025-01-01T00:00:00")))
                .andExpect(jsonPath("$[0].end", equalTo("2025-01-02T00:00:00")))
                .andExpect(jsonPath("$[0].item.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$[0].item.name", equalTo("item")))
                .andExpect(jsonPath("$[0].item.description", equalTo("some item")))
                .andExpect(jsonPath("$[0].item.available", equalTo(true)))
                .andExpect(jsonPath("$[0].item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$[0].booker.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$[0].booker.name", equalTo("booker")))
                .andExpect(jsonPath("$[0].booker.email", equalTo("booker@mail.ru")))
                .andExpect(jsonPath("$[0].status", equalTo("WAITING")));

    }

    @Test
    void getOwnerBookings() throws Exception {

        LocalDateTime now = LocalDateTime.parse("2025-01-01T00:00:00");

        Mockito
                .when(bookingService.getOwnerBookings("WAITING", owner.getId(), now))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner?state=WAITING&now=2025-01-01T00:00:00")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$[0].start", equalTo("2025-01-01T00:00:00")))
                .andExpect(jsonPath("$[0].end", equalTo("2025-01-02T00:00:00")))
                .andExpect(jsonPath("$[0].item.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$[0].item.name", equalTo("item")))
                .andExpect(jsonPath("$[0].item.description", equalTo("some item")))
                .andExpect(jsonPath("$[0].item.available", equalTo(true)))
                .andExpect(jsonPath("$[0].item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$[0].booker.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$[0].booker.name", equalTo("booker")))
                .andExpect(jsonPath("$[0].booker.email", equalTo("booker@mail.ru")))
                .andExpect(jsonPath("$[0].status", equalTo("WAITING")));
    }
}