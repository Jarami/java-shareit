package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapperImpl;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapperImpl;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private BookingService bookingService;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @Mock
    private BookingRepository bookingRepository;

    private User user;
    private User owner;
    private Item item;
    private Booking savedBooking;

    @BeforeEach
    void setup() {
        BookingMapper bookingMapper = new BookingMapperImpl(new ItemMapperImpl(), new UserMapperImpl());
        bookingService = new BookingService(userService, itemService, bookingRepository, bookingMapper);

        user = new User(1L, "user", "user@mail.ru");
        owner = new User(2L, "owner", "owner@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, List.of());
        savedBooking = new Booking(1L, LocalDateTime.parse("2025-02-03T00:00:00"),
                LocalDateTime.parse("2026-02-03T00:00:00"), item, user, BookingStatus.WAITING);

    }

    @Nested
    class CreateBooking {

        @BeforeEach
        void setup() {
            mockUserById(user);
            mockItemById(item);
        }

        @Test
        void givenStartIsAfterEnd_whenCreate_gotBadRequestException() {

            CreateBookingRequest request = new CreateBookingRequest(item.getId(),
                    LocalDateTime.parse("2025-02-03T00:00:00"),
                    LocalDateTime.parse("2024-02-02T00:00:00"));

            assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, user.getId()));
        }

        @Test
        void givenStartIsEqualEnd_whenCreate_gotBadRequestException() {

            CreateBookingRequest request = new CreateBookingRequest(item.getId(),
                    LocalDateTime.parse("2025-02-03T00:00:00"),
                    LocalDateTime.parse("2025-02-03T00:00:00"));

            assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, user.getId()));
        }

        @Test
        void givenUnavailableItem_whenCreate_gotBadRequestException() {

            item.setAvailable(false);

            CreateBookingRequest request = new CreateBookingRequest(item.getId(),
                    LocalDateTime.parse("2025-02-03T00:00:00"),
                    LocalDateTime.parse("2026-02-03T00:00:00"));

            assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, user.getId()));
        }

        @Test
        void givenValidRequest_whenCreate_gotBooking() {

            mockBookingSave();

            CreateBookingRequest request = new CreateBookingRequest(item.getId(),
                    LocalDateTime.parse("2025-02-03T00:00:00"),
                    LocalDateTime.parse("2026-02-03T00:00:00"));

            Booking booking = bookingService.createBooking(request, user.getId());

            Mockito.verify(bookingRepository, Mockito.times(1))
                    .save(Mockito.any(Booking.class));

            assertEquals(booking.getId(), 1L);
            assertEquals(booking.getStart(), LocalDateTime.parse("2025-02-03T00:00:00"));
            assertEquals(booking.getEnd(), LocalDateTime.parse("2026-02-03T00:00:00"));
            assertEquals(booking.getItem(), item);
            assertEquals(booking.getBooker(), user);
            assertEquals(booking.getStatus(), BookingStatus.WAITING);
        }

    }

    @Nested
    class ApproveBooking {

        @Test
        void givenBookingIsAbsent_whenApprove_gotNotFoundException() {

            Mockito
                .when(bookingRepository.findById(2L))
                .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                bookingService.approveBooking(2L, true, user.getId()));
        }

        @Test
        void givenCurrentUserNotTheOwner_whenApprove_gotForbiddenException() {

            mockBookingById(savedBooking);

            assertThrows(ForbiddenException.class, () ->
                bookingService.approveBooking(savedBooking.getId(), true, 1000L));
        }

        @Test
        void givenApprovedByValidUser_whenApprove_gotApproved() {

            mockBookingById(savedBooking);

            bookingService.approveBooking(savedBooking.getId(), true, owner.getId());

            Booking actualBooking = bookingService.findById(savedBooking.getId());

            assertEquals(BookingStatus.APPROVED, actualBooking.getStatus());
        }

        @Test
        void givenRejectedByValidUser_whenApprove_gotApproved() {

            mockBookingById(savedBooking);

            bookingService.approveBooking(savedBooking.getId(), false, owner.getId());

            Booking actualBooking = bookingService.findById(savedBooking.getId());

            assertEquals(BookingStatus.REJECTED, actualBooking.getStatus());
        }
    }

    @Nested
    class GetBookingByIdAndUser {

        @BeforeEach
        void setup() {
            mockBookingById(savedBooking);
        }

        @Test
        void givenOwner_whenGet_gotBooking() {

            mockUserById(owner);

            Booking actualBooking = bookingService.getBookingByIdAndUser(savedBooking.getId(), owner.getId());

            assertEquals(savedBooking.getId(), actualBooking.getId());
        }

        @Test
        void givenBooker_whenGet_gotBooking() {

            mockUserById(user);

            Booking actualBooking = bookingService.getBookingByIdAndUser(savedBooking.getId(), user.getId());

            assertEquals(savedBooking.getId(), actualBooking.getId());
        }

        @Test
        void givenNeitherOwnerNorBooker_whenGet_gotForbiddenException() {
            User someUser = new User(3L, "someUser", "someUser@mail.ru");
            mockUserById(someUser);

            assertThrows(ForbiddenException.class, () ->
                    bookingService.getBookingByIdAndUser(savedBooking.getId(), someUser.getId()));
        }
    }


    @Nested
    class GetCurrentUserBookings {

        LocalDateTime now;

        @BeforeEach
        void setup() {
            mockUserById(user);
            now = LocalDateTime.now();
        }

        @Test
        void givenUserBookings_whenGetAll_gotAll() {

            Mockito
                    .when(bookingRepository.findAllByBookerOrderByStartAsc(user))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getCurrentUserBookings("ALL", user.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetCurrent_gotCurrent() {

            Mockito
                    .when(bookingRepository.findAllByBookerAndStartBeforeAndEndAfterOrderByStartAsc(user, now, now))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getCurrentUserBookings("CURRENT", user.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetPast_gotPast() {

            Mockito
                    .when(bookingRepository.findAllByBookerAndEndBeforeOrderByStartAsc(user, now))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getCurrentUserBookings("PAST", user.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetFuture_gotFuture() {

            Mockito
                    .when(bookingRepository.findAllByBookerAndStartAfterOrderByStartAsc(user, now))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getCurrentUserBookings("FUTURE", user.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetWaiting_gotWaiting() {

            Mockito
                    .when(bookingRepository.findAllByBookerAndStatusOrderByStartAsc(user, BookingStatus.WAITING))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getCurrentUserBookings("WAITING", user.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetRejected_gotRejected() {

            Mockito
                    .when(bookingRepository.findAllByBookerAndStatusOrderByStartAsc(user, BookingStatus.REJECTED))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getCurrentUserBookings("REJECTED", user.getId(), now);

            assertEquals(1, bookings.size());
        }
    }

    @Nested
    class GetOwnerBookings {
        LocalDateTime now;

        @BeforeEach
        void setup() {
            mockUserById(owner);
            now = LocalDateTime.now();
        }

        @Test
        void givenUserBookings_whenGetAll_gotAll() {

            Mockito
                    .when(bookingRepository.findAllByOwnerOrderByStartAsc(owner))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getOwnerBookings("ALL", owner.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetCurrent_gotCurrent() {

            Mockito
                    .when(bookingRepository.findAllCurrentByOwnerOrderByStartAsc(owner, now))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getOwnerBookings("CURRENT", owner.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetPast_gotPast() {

            Mockito
                    .when(bookingRepository.findAllPastByOwnerOrderByStartAsc(owner, now))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getOwnerBookings("PAST", owner.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetFuture_gotFuture() {

            Mockito
                    .when(bookingRepository.findAllByOwnerOrderByStartAsc(owner, now))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getOwnerBookings("FUTURE", owner.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetWaiting_gotWaiting() {

            Mockito
                    .when(bookingRepository.findAllByOwnerAndStatusOrderByStartAsc(owner, BookingStatus.WAITING))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getOwnerBookings("WAITING", owner.getId(), now);

            assertEquals(1, bookings.size());
        }

        @Test
        void givenUserBookings_whenGetRejected_gotRejected() {

            Mockito
                    .when(bookingRepository.findAllByOwnerAndStatusOrderByStartAsc(owner, BookingStatus.REJECTED))
                    .thenReturn(List.of(savedBooking));

            List<Booking> bookings = bookingService.getOwnerBookings("REJECTED", owner.getId(), now);

            assertEquals(1, bookings.size());
        }
    }

    @Test
    void existPastApprovedItemBookingByUser() {

        LocalDateTime now = LocalDateTime.now();

        Mockito
                .when(bookingRepository.existsByItemAndBookerAndStatusAndEndBefore(item, user, BookingStatus.APPROVED, now))
                .thenReturn(true);

        assertTrue(bookingService.existPastApprovedItemBookingByUser(item, user, now));
    }

    private void mockUserById(User user) {
        Mockito
                .when(userService.getById(user.getId()))
                .thenReturn(user);
    }

    private void mockItemById(Item item) {
        Mockito
                .when(itemService.getById(item.getId()))
                .thenReturn(item);
    }

    private void mockBookingById(Booking booking) {
        Mockito
                .when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
    }

    private void mockBookingSave() {
        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0, Booking.class);
                    booking.setId(1L);
                    return booking;
                });
    }
}