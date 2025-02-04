package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private CommentService commentService;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CommentRepository commentRepository;

    private User user;
    private User owner;
    private Item item;
    private Booking savedBooking;

    @BeforeEach
    void setup() {
        commentService = new CommentService(itemService, userService, bookingService, new ItemMapperImpl(),
                commentRepository);

        user = new User(1L, "user", "user@mail.ru");
        owner = new User(2L, "owner", "owner@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, new ArrayList<>());
    }

    @Nested
    class CreateComment {

        @Test
        void givenNoBookingByUser_whenComment_gotBadRequestException() {
            mockItemById(item);
            mockUserById(user);

            LocalDateTime now = LocalDateTime.now();

            Mockito
                    .when(bookingService.existPastApprovedItemBookingByUser(item, user, now))
                    .thenReturn(false);

            CreateCommentRequest request = new CreateCommentRequest("comment");

            assertThrows(BadRequestException.class, () ->
                    commentService.createComment(request, item.getId(), user.getId(), now));
        }

        @Test
        void givenBookingByUser_whenComment_gotBookingCommented() {
            mockItemById(item);
            mockUserById(user);
            mockCommentSave();

            LocalDateTime now = LocalDateTime.now();

            Mockito
                    .when(bookingService.existPastApprovedItemBookingByUser(item, user, now))
                    .thenReturn(true);

            CreateCommentRequest request = new CreateCommentRequest("comment");

            Comment comment = commentService.createComment(request, item.getId(), user.getId(), now);

            assertEquals(item, comment.getItem());
            assertEquals(user, comment.getAuthor());
            assertIterableEquals(List.of(comment), item.getComments());
        }
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

    private void mockCommentSave() {
        Mockito
                .when(commentRepository.save(Mockito.any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0, Comment.class);
                    comment.setId(1L);
                    return comment;
                });
    }
}