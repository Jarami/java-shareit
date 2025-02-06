package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestBody @Valid CreateBookingRequest requestDto) {
		log.info("Create booking {}, userId={}", requestDto, userId);
		return bookingClient.createBooking(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(@PathVariable Long bookingId,
												 @RequestParam Boolean approved,
												 @RequestHeader("X-Sharer-User-Id") Long userId) {

		log.info("Approve booking {}, userId={} ({})", bookingId, userId, approved);
		return bookingClient.approveBooking(bookingId, approved, userId);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBookingInfo(@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBookingInfo(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getCurrentUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
														 @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {

		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

		log.info("Get booking with state {}, userId={}", stateParam, userId);
		return bookingClient.getCurrentUserBookings(userId, state);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
														 @RequestHeader("X-Sharer-User-Id") Long userId) {

		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

		log.info("Get owner bookings with state {}, userId={}", stateParam, userId);

		return bookingClient.getOwnerBookings(userId, state);
	}
}
