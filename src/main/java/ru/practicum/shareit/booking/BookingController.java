package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;

import java.util.List;

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
    public ResponseEntity<List<BookingDto>> getCurrentUserBookings(@RequestParam(required = false, defaultValue = "ALL") String stateValue,
                                                                   @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("getting current user {} bookings with state {}", userId, stateValue);

        List<Booking> bookings = bookingService.getCurrentUserBookings(stateValue, userId);
        return new ResponseEntity<>(mapper.toDto(bookings), HttpStatus.OK);

    }
}
