package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;

    public Comment createComment(@Valid CreateCommentRequest request, Long itemId, Long userId) {

        Item item = itemService.getById(itemId);
        User user = userService.getById(userId);

        log.info("item = {}", item);
        log.info("user = {}", user);
//        List<Booking> bookings = bookingService.getCurrentUserBookings("ALL", userId);
//        bookings.forEach(booking -> log.info("booking = {}", booking));

        if (!bookingService.existPastApprovedItemBookingByUser(item, user)) {
            throw new ForbiddenException("запрещено оставлять комментарий для вещи %s пользователем %s",
                    itemId, userId);
        }

        Comment comment = itemMapper.toComment(request);
        comment.setItem(item);
        comment.setAuthor(user);

        return commentRepository.save(comment);
    }
}
