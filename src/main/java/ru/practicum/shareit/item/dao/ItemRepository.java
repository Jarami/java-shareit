package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    String FIND_BY_OWNER_ID1 = """
            SELECT new ru.practicum.shareit.item.Item(item.id,
                                                      item.owner,
                                                      item.name,
                                                      item.description,
                                                      item.available,
                                                      max(pastBooking.start),
                                                      min(nextBooking.end))
            FROM Item item
            JOIN item.owner owner
            LEFT JOIN Booking pastBooking ON pastBooking.end < :now AND pastBooking.item = item
            LEFT JOIN Booking nextBooking ON nextBooking.start > :now AND nextBooking.item = item
            WHERE owner = :owner
            GROUP BY item.id, item.owner, item.name, item.description, item.available
            """;

    String SEARCH_QUERY = """
            SELECT id as "id",
                   owner_id as "owner_id",
                   name as "name",
                   description as "description",
                   available as "available"
            FROM items
            WHERE available = true
            AND (
                name ilike %:substring%
                OR description ilike %:substring%
            );
            """;

    @Query(value = FIND_BY_OWNER_ID1)
    List<Item> findAllByOwnerWithPastNextBooking(@Param("owner") User owner, @Param("now") LocalDateTime now);

    @Query(value = SEARCH_QUERY, nativeQuery = true)
    List<Item> search(@Param("substring") String substring);
}
