package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperImplTest {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ItemMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ItemMapperImpl();
    }

    @Nested
    class ItemMapping {
        @Test
        void givenNullItem_whenToDto_gotNullDto() {
            Item item = null;
            ItemDto dto = mapper.toDto(item);
            assertNull(dto);
        }

        @Test
        void givenNullLastAndNextBooking_whenToDto_gotNullLastAndNextBookingDto() {
            Item item = new Item();
            ItemDto dto = mapper.toDto(item);

            assertNull(item.getNextBooking());
            assertNull(item.getLastBooking());
        }

        @Test
        void givenNullItemList_whenToDto_gotNullDtoList() {
            List<Item> items = null;
            List<ItemDto> dtos = mapper.toDto(items);
            assertNull(dtos);
        }

        @Test
        void givenValidList_whenToDto_gotValidDtoList() {
            User owner = new User(1L, "owner", "owner@mail.ru");
            List<Item> items = List.of(
                    new Item(2L, owner, "item1", "item1 desc", true, new ArrayList<>()),
                    new Item(3L, owner, "item2", "item2 desc", true, new ArrayList<>()));

            List<ItemDto> dtos = mapper.toDto(items);
            assertEquals(2, dtos.size());
            assertEquals(2L, dtos.get(0).getId());
            assertEquals(3L, dtos.get(1).getId());
        }

        @Test
        void givenValidItem_whenToDto_gotValidDto() {
            User owner = new User(1L, "owner", "owner@mail.ru");
            Item item = new Item(1L, owner, "item", "item desc", true, new ArrayList<>());

            LocalDateTime lastBooking = LocalDateTime.now().minusDays(5);
            item.setLastBooking(lastBooking);

            LocalDateTime nextBooking = LocalDateTime.now().plusDays(5);
            item.setNextBooking(nextBooking);

            User author = new User(2L, "author", "author@mail.ru");
            Comment comment = new Comment(3L, "text", author, item, Instant.now());
            item.getComments().add(comment);

            ItemDto dto = mapper.toDto(item);

            assertEquals(1L, dto.getId());
            assertEquals("item", dto.getName());
            assertEquals("item desc", dto.getDescription());
            assertTrue(dto.isAvailable());
            assertEquals(formatter.format(lastBooking), dto.getLastBooking());
            assertEquals(formatter.format(nextBooking), dto.getNextBooking());

            assertEquals(1, dto.getComments().size());
            assertEquals(3L, dto.getComments().get(0).getId());
        }

        @Test
        void givenNullRequest_whenToItem_gotNullItem() {
            CreateItemRequest request = null;
            Item item = mapper.toItem(request);
            assertNull(item);
        }

        @Test
        void givenRequestWithoutAvailable_whenToItem_gotUnavailableItem() {
            CreateItemRequest request = new CreateItemRequest("item", "item desc", null, null);
            Item item = mapper.toItem(request);
            assertFalse(item.isAvailable());
        }

        @Test
        void givenValidRequest_whenToItem_gotValidItem() {
            CreateItemRequest request = new CreateItemRequest("item", "item desc", false, null);
            Item item = mapper.toItem(request);

            assertEquals("item", item.getName());
            assertEquals("item desc", item.getDescription());
            assertFalse(item.isAvailable());
        }
    }

    @Nested
    class CommentMapping {

        @Test
        void givenNullRequest_whenToComment_gotNullComment() {
            CreateCommentRequest request = null;
            Comment comment = mapper.toComment(request);
            assertNull(comment);
        }

        @Test
        void givenValidRequest_whenToComment_gotNullComment() {
            CreateCommentRequest request = new CreateCommentRequest("text");
            Comment comment = mapper.toComment(request);
            assertEquals("text", comment.getText());
        }

        @Test
        void givenNullComment_whenToDto_gotNullDto() {
            Comment comment = null;
            CommentDto dto = mapper.toCommentDto(comment);
            assertNull(dto);
        }

        @Test
        void givenNullAuthor_whenToDto_gotNullAuthorName() {
            Comment comment = new Comment(5L, "text", null, null, Instant.now());
            CommentDto dto = mapper.toCommentDto(comment);

            assertNull(dto.getAuthorName());
        }

        @Test
        void givenNoCreated_whenToDto_gotNullCreated() {
            Comment comment = new Comment(5L, "text", null, null, null);
            CommentDto dto = mapper.toCommentDto(comment);

            assertNull(dto.getCreated());
        }

        @Test
        void givenValidComment_whenToDto_gotValidDto() {
            User author = new User(6L, "author", "author@mail.ru");
            User owner = new User(7L, "owner", "owner@mail.ru");
            Item item = new Item(8L, owner, "item", "item desc", true, new ArrayList<>());
            Comment comment = new Comment(5L, "text", author, item, Instant.now());
            item.getComments().add(comment);

            CommentDto dto = mapper.toCommentDto(comment);

            assertEquals(5L, dto.getId());
            assertEquals("text", dto.getText());
            assertEquals("author", dto.getAuthorName());
        }

        @Test
        void givenValidCommentList_whenToDto_gotValidDtoList() {
            User owner = new User(7L, "owner", "owner@mail.ru");
            Item item = new Item(8L, owner, "item", "item desc", true, new ArrayList<>());

            User author1 = new User(6L, "author1", "author1@mail.ru");
            Comment comment1 = new Comment(5L, "text", author1, item, Instant.now());

            User author2 = new User(7L, "author2", "author2@mail.ru");
            Comment comment2 = new Comment(6L, "text", author2, item, Instant.now());

            item.getComments().add(comment1);
            item.getComments().add(comment2);

            List<CommentDto> dtos = mapper.toCommentDto(List.of(comment1, comment2));

            assertEquals(2, dtos.size());
            assertEquals(5L, dtos.get(0).getId());
            assertEquals(6L, dtos.get(1).getId());
        }

        @Test
        void givenNullCommentList_whenToDto_gotNullDtoList() {
            List<Comment> commentList = null;
            List<CommentDto> dtoList = mapper.toCommentDto(commentList);
            assertNull(dtoList);
        }
    }
}