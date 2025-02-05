package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingMapper mapper;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {

        Booking booking = bookingService.createBooking(request, userId);
        return new ResponseEntity<>(mapper.toDto(booking), HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@PathVariable Long bookingId,
                                                     @RequestParam Boolean approved,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {

        Booking booking = bookingService.approveBooking(bookingId, approved, userId);
        return new ResponseEntity<>(mapper.toDto(booking), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingInfo(@PathVariable Long bookingId,
                                                     @RequestHeader("X-Sharer-User-Id") Long userId) {

            Booking booking = bookingService.getBookingByIdAndUser(bookingId, userId);
            return new ResponseEntity<>(mapper.toDto(booking), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<BookingDto>> getCurrentUserBookings(@RequestParam(required = false, defaultValue = "ALL") String state,
                                                                   @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now,
                                                                   @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("getting current user {} bookings with state {} at {}", userId, state, now);

        List<Booking> bookings = bookingService.getCurrentUserBookings(state, userId, now);
        return new ResponseEntity<>(mapper.toDto(bookings), HttpStatus.OK);

    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getOwnerBookings(@RequestParam(required = false, defaultValue = "ALL") String state,
                                                             @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now,
                                                             @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("getting owner {} bookings with state {} at {}", userId, state, now);

        List<Booking> bookings = bookingService.getOwnerBookings(state, userId, now);
        return new ResponseEntity<>(mapper.toDto(bookings), HttpStatus.OK);
    }
}
