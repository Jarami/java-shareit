package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import ru.practicum.shareit.item.ItemMapperImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapperImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({UserMapperImpl.class, ItemMapperImpl.class, BookingMapperImpl.class})
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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setup() {
        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(2);
        owner = new User(1L, "owner", "owner@mail.ru");
        booker = new User(2L, "booker", "booker@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, new ArrayList<>());
        booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);
    }

    @Nested
    class CreateBooking {

        @Test
        void givenNoItemId_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(null, start, end);

            mvc.perform(post("/bookings")
                    .content(mapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoStart_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(item.getId(), null, end);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenStartInPast_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(item.getId(), LocalDateTime.now().minusDays(1), end);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoEnd_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(item.getId(), start, null);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEndInPast_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(item.getId(), start, LocalDateTime.now().minusDays(1));

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenCreateBooking_gotCreated() throws Exception {

            CreateBookingRequest request = new CreateBookingRequest(item.getId(), start, end);

            Mockito
                    .when(bookingService.createBooking(request, owner.getId()))
                    .thenReturn(booking);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(booking.getId()), Long.class))
                    .andExpect(jsonPath("$.start", equalTo(formatter.format(start))))
                    .andExpect(jsonPath("$.end", equalTo(formatter.format(end))))
                    .andExpect(jsonPath("$.item.id", equalTo(item.getId()), Long.class))
                    .andExpect(jsonPath("$.item.name", equalTo(item.getName())))
                    .andExpect(jsonPath("$.item.description", equalTo(item.getDescription())))
                    .andExpect(jsonPath("$.item.available", equalTo(item.isAvailable())))
                    .andExpect(jsonPath("$.item.comments", equalTo(List.of())))
                    .andExpect(jsonPath("$.booker.id", equalTo(booker.getId()), Long.class))
                    .andExpect(jsonPath("$.booker.name", equalTo(booker.getName())))
                    .andExpect(jsonPath("$.booker.email", equalTo(booker.getEmail())))
                    .andExpect(jsonPath("$.status", equalTo(booking.getStatus().toString())));
        }
    }

    @Test
    void approveBooking() throws Exception {

        Mockito
                .when(bookingService.approveBooking(booking.getId(), true, owner.getId()))
                .thenReturn(booking);

        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", equalTo(formatter.format(start))))
                .andExpect(jsonPath("$.end", equalTo(formatter.format(end))))
                .andExpect(jsonPath("$.item.id", equalTo(item.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", equalTo(item.getName())))
                .andExpect(jsonPath("$.item.description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$.item.available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$.item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$.booker.id", equalTo(booker.getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", equalTo(booker.getName())))
                .andExpect(jsonPath("$.booker.email", equalTo(booker.getEmail())))
                .andExpect(jsonPath("$.status", equalTo(booking.getStatus().toString())));
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
                .andExpect(jsonPath("$.id", equalTo(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", equalTo(formatter.format(start))))
                .andExpect(jsonPath("$.end", equalTo(formatter.format(end))))
                .andExpect(jsonPath("$.item.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.item.name", equalTo(item.getName())))
                .andExpect(jsonPath("$.item.description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$.item.available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$.item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$.booker.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$.booker.name", equalTo(booker.getName())))
                .andExpect(jsonPath("$.booker.email", equalTo(booker.getEmail())))
                .andExpect(jsonPath("$.status", equalTo(booking.getStatus().toString())));
    }

    @Test
    void getCurrentUserBookings() throws Exception {

        LocalDateTime now = LocalDateTime.parse("2025-01-01T00:00:00");

        Mockito
                .when(bookingService.getCurrentUserBookings("WAITING", owner.getId(), now))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings?state=WAITING&now=2025-01-01T00:00:00")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", equalTo(formatter.format(start))))
                .andExpect(jsonPath("$[0].end", equalTo(formatter.format(end))))
                .andExpect(jsonPath("$[0].item.id", equalTo(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", equalTo(item.getName())))
                .andExpect(jsonPath("$[0].item.description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$[0].item.available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$[0].item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$[0].booker.id", equalTo(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", equalTo(booker.getName())))
                .andExpect(jsonPath("$[0].booker.email", equalTo(booker.getEmail())))
                .andExpect(jsonPath("$[0].status", equalTo(booking.getStatus().toString())));

    }

    @Test
    void getOwnerBookings() throws Exception {

        LocalDateTime now = LocalDateTime.parse("2025-01-01T00:00:00");

        Mockito
                .when(bookingService.getOwnerBookings("WAITING", owner.getId(), now))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner?state=WAITING&now=2025-01-01T00:00:00")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", equalTo(formatter.format(start))))
                .andExpect(jsonPath("$[0].end", equalTo(formatter.format(end))))
                .andExpect(jsonPath("$[0].item.id", equalTo(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", equalTo(item.getName())))
                .andExpect(jsonPath("$[0].item.description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$[0].item.available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$[0].item.comments", equalTo(List.of())))
                .andExpect(jsonPath("$[0].booker.id", equalTo(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", equalTo(booker.getName())))
                .andExpect(jsonPath("$[0].booker.email", equalTo(booker.getEmail())))
                .andExpect(jsonPath("$[0].status", equalTo(booking.getStatus().toString())));
    }
}