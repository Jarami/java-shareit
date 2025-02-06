package ru.practicum.shareit.itemRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("""
            SELECT r
            FROM ItemRequest as r
            LEFT JOIN FETCH r.items
            WHERE r.requester = :requester
            ORDER BY created DESC""")
    List<ItemRequest> findAllByRequesterWithItems(User requester);

    @Query("""
            SELECT r
            FROM ItemRequest as r
            WHERE r.requester <> :requester
            ORDER BY created DESC""")
    List<ItemRequest> findAllRequesterNotOrderByCreatedDesc(User requester);

    @Query("""
            SELECT r
            FROM ItemRequest as r
            LEFT JOIN FETCH r.items
            WHERE r.id = :requestId""")
    Optional<ItemRequest> findByIdWithItems(Long requestId);
}
