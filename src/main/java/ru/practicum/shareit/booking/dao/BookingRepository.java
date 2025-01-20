package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerOrderByStartAsc(User booker);

    List<Booking> findAllByBookerAndStartBeforeAndEndAfterOrderByStartAsc(User booker, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByBookerAndEndBeforeOrderByStartAsc(User booker, LocalDateTime end);

    List<Booking> findAllByBookerAndStartAfterOrderByStartAsc(User booker, LocalDateTime end);

    List<Booking> findAllByBookerAndStatusOrderByStartAsc(User booker, BookingStatus status);

    @Query("""
            SELECT b
            FROM Booking as b
            JOIN b.item as i
            WHERE i.owner = ?1
            ORDER BY start ASC""")
    List<Booking> findAllByOwnerOrderByStartAsc(User owner);

    @Query("""
            SELECT b
            FROM Booking as b
            JOIN b.item as i
            WHERE i.owner = ?1
            AND b.start < ?2 AND b.end > ?3
            ORDER BY start ASC""")
    List<Booking> findAllByOwnerAndStartBeforeAndEndAfterOrderByStartAsc(User owner, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT b
            FROM Booking as b
            JOIN b.item as i
            WHERE i.owner = ?1
            AND b.end < ?2
            ORDER BY start ASC""")
    List<Booking> findAllByOwnerAndEndBeforeOrderByStartAsc(User owner, LocalDateTime end);

    @Query("""
            SELECT b
            FROM Booking as b
            JOIN b.item as i
            WHERE i.owner = ?1
            AND b.start > ?2
            ORDER BY start ASC""")
    List<Booking> findAllByOwnerAndStartAfterOrderByStartAsc(User owner, LocalDateTime start);

    @Query("""
            SELECT b
            FROM Booking as b
            JOIN b.item as i
            WHERE i.owner = ?1
            AND b.status = ?2
            ORDER BY start ASC""")
    List<Booking> findAllByOwnerAndStatusOrderByStartAsc(User owner, BookingStatus status);
}
