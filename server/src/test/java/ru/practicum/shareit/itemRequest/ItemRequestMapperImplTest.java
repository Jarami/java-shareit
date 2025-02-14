package ru.practicum.shareit.itemRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemRequestMapperImplTest {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ItemRequestMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ItemRequestMapperImpl();
    }

    @Nested
    class ToDto {

        @Test
        void givenNullItem_whenToDto_gotNullDto() {
            ItemRequest itemRequest = null;
            ItemRequestDto dto = mapper.toDto(itemRequest);
            assertNull(dto);
        }

        @Test
        void givenNullCreated_whenToDto_gotNullCreated() {
            ItemRequest itemRequest = new ItemRequest();
            ItemRequestDto dto = mapper.toDto(itemRequest);

            assertNull(dto.getCreated());
        }

        @Test
        void givenValidItem_whenToDto_gotValidDto() {
            User owner1 = new User(1L, "owner1", "owner1@mail.ru");
            Item item1 = new Item(2L, owner1, "item1", "item1 desc", true, new ArrayList<>());

            User owner2 = new User(3L, "owner2", "owner2@mail.ru");
            Item item2 = new Item(4L, owner2, "item2", "item2 desc", true, new ArrayList<>());

            User requester = new User(5L, "requester", "requester@mail.ru");
            ItemRequest itemRequest = new ItemRequest(6L, "item request", requester, new ArrayList<>(), Instant.now());
            itemRequest.getItems().add(item1);
            itemRequest.getItems().add(item2);

            ItemRequestDto dto = mapper.toDto(itemRequest);

            assertEquals(6L, dto.getId());
            assertEquals("item request", dto.getDescription());
            assertEquals(2, dto.getItems().size());

            assertEquals(2L, dto.getItems().get(0).getId());
            assertEquals(4L, dto.getItems().get(1).getId());
        }

        @Test
        void givenNullItemRequestList_whenToDto_gotNullDtoList() {
            List<ItemRequest> itemRequests = null;
            List<ItemRequestDto> dtos = mapper.toDto(itemRequests);
            assertNull(dtos);
        }

        @Test
        void givenValidList_whenToDto_gotValidDtoList() {
            List<ItemRequest> itemRequests = List.of(
                new ItemRequest(1L, null, null, null, null),
                new ItemRequest(2L, null, null, null, null));

            List<ItemRequestDto> dtos = mapper.toDto(itemRequests);
            assertEquals(2, dtos.size());
            assertEquals(1L, dtos.get(0).getId());
            assertEquals(2L, dtos.get(1).getId());
        }
    }

    @Nested
    class CreateRequestMapping {
        @Test
        void givenNullRequest_whenToItemRequest_gotNullItemRequset() {
            CreateItemRequestRequest request = null;
            User user = null;
            ItemRequest itemRequest = mapper.toItemRequest(request, user);
            assertNull(itemRequest);
        }

        @Test
        void givenNullRequestButUserExist_whenToItemRequest_gotItemRequestWithRequesterWithoutDesc() {
            CreateItemRequestRequest request = null;
            User user = new User(1L, "requester", "requester@mail.ru");

            ItemRequest itemRequest = mapper.toItemRequest(request, user);

            assertNull(itemRequest.getDescription());
            assertEquals(1L, itemRequest.getRequester().getId());
        }

        @Test
        void givenValidRequestAndUser_whenToItemRequest_gotValidRequest() {
            CreateItemRequestRequest request = new CreateItemRequestRequest("item request");
            User requester = new User(1L, "requester", "requester@mail.ru");

            ItemRequest itemRequest = mapper.toItemRequest(request, requester);

            assertEquals("item request", itemRequest.getDescription());
            assertEquals(1L, itemRequest.getRequester().getId());
            assertNull(itemRequest.getId());
        }
    }

    @Nested
    class ItemMapping {

        @Test
        void givenNullItem_whenToDto_gotNullDto() {
            Item item = null;
            ItemRequestDto.ItemDto dto = mapper.toItemDto(item);
            assertNull(dto);
        }

        @Test
        void givenItemWithoutOwner_whenToItem_gotItemDtoWithoutOwnerId() {
            Item item = new Item(1L, null, "item", "item desc", true, new ArrayList<>());
            ItemRequestDto.ItemDto dto = mapper.toItemDto(item);
            assertNull(dto.getOwnerId());
        }

        @Test
        void givenValidItem_whenToItem_gotValidItemDto() {
            User owner = new User(1L, "owner", "owner@mail.ru");
            Item item = new Item(2L, owner, "item", "item desc", true, new ArrayList<>());

            ItemRequestDto.ItemDto dto = mapper.toItemDto(item);
            assertEquals(1L, dto.getOwnerId());
            assertEquals(2L, dto.getId());
            assertEquals("item", dto.getName());
        }

        @Test
        void givenNullItemRequestList_whenToDto_gotNullDtoList() {
            List<ItemRequest> itemRequests = null;
            List<ItemRequestDto> dtos = mapper.toDto(itemRequests);
            assertNull(dtos);
        }

    }
}