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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingClient bookingClient;

    @Autowired
    private MockMvc mvc;

    LocalDateTime start;
    LocalDateTime end;

    @BeforeEach
    void setup() {
        start = LocalDate.now().atStartOfDay().plusDays(1);
        end = LocalDate.now().atStartOfDay().plusDays(2);
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
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoStart_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(1L, null, end);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenStartInPast_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(1L, LocalDateTime.now().minusDays(1), end);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoEnd_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(1L, start, null);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEndInPast_whenCreateItem_gotValidationException() throws Exception {
            CreateBookingRequest request = new CreateBookingRequest(1L, start, LocalDateTime.now().minusDays(1));

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenCreateBooking_gotCreated() throws Exception {

            CreateBookingRequest request = new CreateBookingRequest(1L, start, end);

            MultiValueMap<String, String> headers = CollectionUtils.toMultiValueMap(Map.of(
                    "Content-Type", List.of("application/json")
            ));
            ResponseEntity<Object> response = new ResponseEntity<>(Map.of(), headers, HttpStatus.CREATED);

            Mockito
                    .when(bookingClient.createBooking(any(Long.class), any(CreateBookingRequest.class)))
                    .thenReturn(response);

            mvc.perform(post("/bookings")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void approveBooking() {
    }

    @Test
    void getBookingInfo() {
    }

    @Test
    void getCurrentUserBookings() {
    }

    @Test
    void getOwnerBookings() {
    }
}