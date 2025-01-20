package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.BadRequest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BookingService {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingRepository repo;
    private final BookingMapper mapper;

    public Booking createBooking(@Valid CreateBookingRequest request, long userId) {

        log.info("create booking {}", request);

        User booker = userService.getById(userId);
        Item item = itemService.getById(request.getItemId());

        if (request.getStart().isAfter(request.getEnd())) {
            throw new BadRequest("Начало бронирования не должно быть после его окончания");
        }

        if (request.getStart().isEqual(request.getEnd())) {
            throw new BadRequest("Начало бронирования не должно совпадать с его окончанием");
        }

        if (!item.isAvailable()) {
            throw new BadRequest("Вещь с id = %s недоступна для бронирования", item.getId());
        }

        Booking booking = mapper.toBooking(request);
        booking.setBooker(booker);
        booking.setItem(item);

        Booking createdBooking = repo.save(booking);
        log.info("created booking {}", createdBooking);

        return createdBooking;
    }

    public Booking approveBooking(Long bookingId, boolean approved, long userId) {

        log.info("{} book {} with user {}", approved ? "approving" : "rejecting", bookingId, userId);

        Booking booking = findById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Пользователю %s запрещено вносить изменения в бронирование %s", userId, bookingId);
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return repo.save(booking);
    }

    private Booking findById(Long bookingId) {
        return repo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("не найдено бронирование с id = %s", bookingId));
    }
}
