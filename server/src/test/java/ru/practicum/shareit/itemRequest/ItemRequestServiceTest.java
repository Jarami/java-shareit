package ru.practicum.shareit.itemRequest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    private ItemRequestService itemRequestService;

    @Mock
    private UserService userService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    private User user;
    private User owner;
    private Item item;

    @BeforeEach
    void setup() {
        itemRequestService = new ItemRequestService(userService, new ItemRequestMapperImpl(), itemRequestRepository);
        user = new User(1L, "user", "user@mail.ru");
        owner = new User(2L, "owner", "owner@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, new ArrayList<>());
    }

    @Test
    void createItemRequest() {
        mockUserById(user);
        mockItemRequestSave();

        CreateItemRequestRequest request = new CreateItemRequestRequest("some item request");
        ItemRequest actualItemReq = itemRequestService.createItemRequest(request, user.getId());

        assertEquals(1000L, actualItemReq.getId());
        assertEquals(user, actualItemReq.getRequester());

        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .save(Mockito.any(ItemRequest.class));

    }

    @Test
    void getUserRequests() {
        mockUserById(user);

        List<ItemRequest> reqs = List.of(
                new ItemRequest(1L, "first request", user, new ArrayList<>(), getCreated("2025-01-01T00:00:00")),
                new ItemRequest(2L, "second request", user, new ArrayList<>(), getCreated("2025-01-02T00:00:00")));

        Mockito
                .when(itemRequestRepository.findAllByRequesterWithItems(user))
                .thenReturn(reqs);

        List<ItemRequest> actualRequests = itemRequestService.getUserRequests(user.getId());

        assertEquals(reqs.size(), actualRequests.size());
        assertEquals(reqs.get(0).getDescription(), actualRequests.get(0).getDescription());
        assertEquals(reqs.get(1).getDescription(), actualRequests.get(1).getDescription());
    }

    @Test
    void getAllRequestsFromOthers() {
        mockUserById(user);

        List<ItemRequest> reqs = List.of(
                new ItemRequest(1L, "first request", user, new ArrayList<>(), getCreated("2025-01-01T00:00:00")),
                new ItemRequest(2L, "second request", user, new ArrayList<>(), getCreated("2025-01-02T00:00:00")));

        Mockito
                .when(itemRequestRepository.findAllRequesterNotOrderByCreatedDesc(user))
                .thenReturn(reqs);

        List<ItemRequest> actualRequests = itemRequestService.getAllRequestsFromOthers(user.getId());

        assertEquals(reqs.size(), actualRequests.size());
        assertEquals(reqs.get(0).getDescription(), actualRequests.get(0).getDescription());
        assertEquals(reqs.get(1).getDescription(), actualRequests.get(1).getDescription());
    }

    @Test
    void getByIdWithItems() {
        ItemRequest req = new ItemRequest(1L, "first request", user, new ArrayList<>(), getCreated("2025-01-01T00:00:00"));

        Mockito
                .when(itemRequestRepository.findByIdWithItems(1L))
                .thenReturn(Optional.of(req));

        assertEquals(req, itemRequestService.getByIdWithItems(1L));
    }

    @Test
    void givenNoRequestById_whenGetById_gotNotFoundException() {
            Mockito
                    .when(itemRequestRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    itemRequestService.getById(1L));
    }

    @Test
    void givenRequestById_whenGetById_gotIt() {

        ItemRequest request = new ItemRequest(2L, null, null, null, null);

        Mockito
                .when(itemRequestRepository.findById(2L))
                .thenReturn(Optional.of(request));

        ItemRequest actualRequest = itemRequestService.getById(2L);
        assertEquals(2L, actualRequest.getId());
    }

    private void mockUserById(User user) {
        Mockito
                .when(userService.getById(user.getId()))
                .thenReturn(user);
    }

    private void mockItemRequestSave() {
        Mockito
                .when(itemRequestRepository.save(Mockito.any(ItemRequest.class)))
                .thenAnswer(invocation -> {
                    ItemRequest req = invocation.getArgument(0, ItemRequest.class);
                    req.setId(1000L);
                    return req;
                });
    }

    private Instant getCreated(String text) {
        return LocalDateTime.parse(text).atZone(ZoneId.of("UTC")).toInstant();
    }
}