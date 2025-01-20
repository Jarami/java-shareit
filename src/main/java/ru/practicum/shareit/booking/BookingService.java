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
import java.util.List;

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

    public Booking getBookingByIdAndUser(Long bookingId, Long userId) {

        Booking booking = findById(bookingId);
        User user = userService.getById(userId);
        User owner = booking.getItem().getOwner();
        User booker = booking.getBooker();

        if (user.equals(owner) || user.equals(booker)) {
            return booking;
        } else {
            throw new ForbiddenException("запрещено просматривать информацию о бронировании {}", bookingId);
        }
    }

    public List<Booking> getCurrentUserBookings(String stateValue, Long userId) {

        log.info("getting bookings for user {} with state {}", userId, stateValue);

        LocalDateTime now = LocalDateTime.now();

        FilterBookingState state = FilterBookingState.valueOf(stateValue);
        User user = userService.getById(userId);

        List<Booking> bookings = switch (state) {
            case ALL -> repo.findAllByBookerOrderByStartAsc(user);
            case CURRENT -> repo.findAllByBookerAndStartBeforeAndEndAfterOrderByStartAsc(user, now, now);
            case PAST -> repo.findAllByBookerAndEndBeforeOrderByStartAsc(user, now);
            case FUTURE -> repo.findAllByBookerAndStartAfterOrderByStartAsc(user, now);
            case WAITING -> repo.findAllByBookerAndStatusOrderByStartAsc(user, BookingStatus.WAITING);
            case REJECTED -> repo.findAllByBookerAndStatusOrderByStartAsc(user, BookingStatus.REJECTED);
        };

        log.info("found {} booking(s)", bookings.size());

        return bookings;
    }

    public List<Booking> getOwnerBookings(String stateValue, Long userId) {

        log.info("getting bookings for owner {} with state {}", userId, stateValue);

        LocalDateTime now = LocalDateTime.now();

        FilterBookingState state = FilterBookingState.valueOf(stateValue);
        User owner = userService.getById(userId);

        List<Booking> bookings = switch (state) {
            case ALL -> repo.findAllByOwnerOrderByStartAsc(owner);
            case CURRENT -> repo.findAllCurrentByOwnerOrderByStartAsc(owner, now);
            case PAST -> repo.findAllPastByOwnerOrderByStartAsc(owner, now);
            case FUTURE -> repo.findAllByOwnerOrderByStartAsc(owner, now);
            case WAITING -> repo.findAllByOwnerAndStatusOrderByStartAsc(owner, BookingStatus.WAITING);
            case REJECTED -> repo.findAllByOwnerAndStatusOrderByStartAsc(owner, BookingStatus.REJECTED);
        };

        log.info("found {} booking(s)", bookings.size());

        return bookings;
    }

    public boolean existPastApprovedItemBookingByUser(Item item, User user) {
        return repo.existsByItemAndBookerAndStatusAndEndBefore(item, user, BookingStatus.APPROVED, LocalDateTime.now());
    }

    private Booking findById(Long bookingId) {
        return repo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("не найдено бронирование с id = %s", bookingId));
    }
}
