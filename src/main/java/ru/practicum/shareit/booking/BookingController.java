package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingMapper mapper;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestBody CreateBookingRequest request,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {

        Booking booking = bookingService.createBooking(request, userId);
        log.info("booking = {}", booking);
        BookingDto dto = mapper.toDto(booking);
        log.info("booking dto = {}", dto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}
