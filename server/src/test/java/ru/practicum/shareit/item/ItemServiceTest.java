package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemLastNextBookDate;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.itemRequest.ItemRequest;
import ru.practicum.shareit.itemRequest.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestService itemRequestService;

    private User user;
    private User owner;
    private Item item;

    @BeforeEach
    void setup() {
        itemService = new ItemService(userService, itemRepository, new ItemMapperImpl(), commentRepository, itemRequestService);
        user = new User(1L, "user", "user@mail.ru");
        owner = new User(2L, "owner", "owner@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, new ArrayList<>());
    }

    @Nested
    class CreateItem {

        @Test
        void givenCreateRequest_whenCreate_gotCreated() {
            mockUserById(owner);
            mockItemSave();

            CreateItemRequest request = new CreateItemRequest("item", "some item", true, null);
            Item actualItem = itemService.createItem(request, owner.getId());

            assertEquals(1L, actualItem.getId());
            assertEquals(owner, actualItem.getOwner());

            Mockito.verify(itemRepository, Mockito.times(1))
                    .save(Mockito.any(Item.class));
        }

        @Test
        void givenCreateRequestWithRequestId_whenCreate_gotCreatedWithRequest() {
            mockUserById(owner);
            mockItemSave();

            ItemRequest itemRequest = new ItemRequest(1L, "some request", user, new ArrayList<>(), getInstant("2025-01-01T00:00:00"));
            mockRequestById(itemRequest);

            CreateItemRequest request = new CreateItemRequest("item", "some item", true, itemRequest.getId());
            Item actualItem = itemService.createItem(request, owner.getId());

            assertEquals(1L, actualItem.getId());
            assertEquals(owner, actualItem.getOwner());
            assertEquals(itemRequest, actualItem.getItemRequest());

            Mockito.verify(itemRepository, Mockito.times(1))
                    .save(Mockito.any(Item.class));
        }
    }

    @Nested
    class UpdateItem {

        @Test
        void givenNotOwner_whenUpdateItem_gotForbiddenException() {

            mockUserById(user);
            mockItemById(item);

            UpdateItemRequest request = new UpdateItemRequest("other item", "some other item", false);

            assertThrows(ForbiddenException.class, () ->
                    itemService.updateItem(request, item.getId(), user.getId()));
        }

        @Test
        void givenNewName_whenUpdateItem_gotNewName() {

            mockUserById(owner);
            mockItemById(item);
            mockItemUpdate(item);

            UpdateItemRequest request = new UpdateItemRequest("other item", null, null);

            itemService.updateItem(request, item.getId(), owner.getId());

            Item actualItem = itemService.getById(item.getId());
            assertEquals("other item", actualItem.getName());
            assertEquals(item.getDescription(), actualItem.getDescription());
            assertEquals(item.isAvailable(), actualItem.isAvailable());
        }

        @Test
        void givenNewDesc_whenUpdateItem_gotNewDesc() {

            mockUserById(owner);
            mockItemById(item);
            mockItemUpdate(item);

            UpdateItemRequest request = new UpdateItemRequest(null, "some new item", null);

            itemService.updateItem(request, item.getId(), owner.getId());

            Item actualItem = itemService.getById(item.getId());
            assertEquals(item.getName(), actualItem.getName());
            assertEquals("some new item", actualItem.getDescription());
            assertTrue(actualItem.isAvailable());
        }

        @Test
        void givenNewAvailable_whenUpdateItem_gotNewAvailable() {

            mockUserById(owner);
            mockItemById(item);
            mockItemUpdate(item);

            UpdateItemRequest request = new UpdateItemRequest(null, null, false);

            itemService.updateItem(request, item.getId(), owner.getId());

            Item actualItem = itemService.getById(item.getId());
            assertEquals(item.getName(), actualItem.getName());
            assertEquals(item.getDescription(), actualItem.getDescription());
            assertFalse(actualItem.isAvailable());
        }
    }

    @Nested
    class GetById {

        @Test
        void givenExistingItem_whenGetById_gotIt() {
            mockItemById(item);

            Item actualItem = itemService.getById(item.getId());

            assertEquals(item, actualItem);
        }

        @Test
        void givenAbsentItem_whenGetById_gotNonFoundException() {

            Mockito
                    .when(itemRepository.findById(item.getId() + 1))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    itemService.getById(item.getId() + 1));
        }
    }

    @Test
    void getByUserId() {

        Item item1 = new Item(2L, owner, "item2", "some item2", true, new ArrayList<>());
        Item item2 = new Item(3L, owner, "item3", "some item3", true, new ArrayList<>());

        mockUserById(owner);

        ItemLastNextBookDate date1 = new ItemLastNextBookDate() {
            public Long getId() {
                return 2L;
            }

            public LocalDateTime getLastBooking() {
                return LocalDateTime.parse("2025-01-01T00:00:00");
            }

            public LocalDateTime getNextBooking() {
                return LocalDateTime.parse("2025-01-02T00:00:00");
            }
        };

        ItemLastNextBookDate date2 = new ItemLastNextBookDate() {
            public Long getId() {
                return 3L;
            }

            public LocalDateTime getLastBooking() {
                return LocalDateTime.parse("2025-01-03T00:00:00");
            }

            public LocalDateTime getNextBooking() {
                return LocalDateTime.parse("2025-01-04T00:00:00");
            }
        };

        LocalDateTime now = LocalDateTime.now();

        Mockito
                .when(itemRepository.getLastAndNextBookingDate(owner, now))
                .thenReturn(List.of(date2, date1)); // обратный порядок
        Mockito
                .when(itemRepository.findAllByOwnerWithComments(owner))
                .thenReturn(List.of(item1, item2));

        List<Item> items = itemService.getByUserId(owner.getId(), now);

        assertEquals(2L, items.get(0).getId());
        assertEquals(LocalDateTime.parse("2025-01-01T00:00:00"), items.get(0).getLastBooking());
        assertEquals(LocalDateTime.parse("2025-01-02T00:00:00"), items.get(0).getNextBooking());

        assertEquals(3L, items.get(1).getId());
        assertEquals(LocalDateTime.parse("2025-01-03T00:00:00"), items.get(1).getLastBooking());
        assertEquals(LocalDateTime.parse("2025-01-04T00:00:00"), items.get(1).getNextBooking());
    }

    @Nested
    class Search {
        @Test
        void givenNull_whenSearch_gotEmptyList() {
            List<Item> items = itemService.search(null);
            assertTrue(items.isEmpty());
        }

        @Test
        void givenEmptyString_whenSearch_gotEmptyList() {
            List<Item> items = itemService.search("");
            assertTrue(items.isEmpty());
        }

        @Test
        void givenValidString_whenSearch_gotItemList() {
            Item item1 = new Item(2L, owner, "item2", "some item2", true, new ArrayList<>());
            Item item2 = new Item(3L, owner, "item3", "some item3", true, new ArrayList<>());

            Mockito
                    .when(itemRepository.search("abc"))
                    .thenReturn(List.of(item1, item2));

            List<Item> items = itemService.search("abc");

            assertIterableEquals(List.of(item1, item2), items);
        }
    }

    @Nested
    class DeleteById {
        @Test
        void givenNotOwnerUser_whenDeleteItem_gotForbiddenException() {
            mockUserById(user);
            mockItemById(item);

            assertThrows(ForbiddenException.class, () ->
                    itemService.deleteById(item.getId(), user.getId()));
        }

        @Test
        void givenOwnerUser_whenDeleteItem_gotDeleted() {
            mockUserById(owner);
            mockItemById(item);

            itemService.deleteById(item.getId(), owner.getId());

            Mockito.verify(itemRepository, Mockito.times(1))
                    .deleteById(item.getId());
        }
    }

    private void mockUserById(User user) {
        Mockito
                .when(userService.getById(user.getId()))
                .thenReturn(user);
    }

    private void mockRequestById(ItemRequest request) {
        Mockito
                .when(itemRequestService.getById(request.getId()))
                .thenReturn(request);
    }

    private void mockItemById(Item item) {
        Mockito
                .when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
    }

    private void mockItemSave() {
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0, Item.class);
                    item.setId(1L);
                    return item;
                });
    }

    private void mockItemUpdate(Item item) {
        Mockito
                .when(itemRepository.save(item))
                .thenReturn(item);
    }

    private Instant getInstant(String text) {
        return LocalDateTime.parse(text).atZone(ZoneId.of("UTC")).toInstant();
    }
}