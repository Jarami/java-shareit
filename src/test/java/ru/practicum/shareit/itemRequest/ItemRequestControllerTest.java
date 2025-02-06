package ru.practicum.shareit.itemRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;
import ru.practicum.shareit.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({ItemRequestMapperImpl.class})
@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private User user;
    private User owner;
    private ItemRequest itemRequest;
    private Instant created;

    @BeforeEach
    void setup() {
        user = new User(1L, "user", "user@mail.ru");
        owner = new User(2L, "owner", "owner@mail.ru");
        created = LocalDateTime.parse("2025-01-01T00:00:00").atZone(ZoneId.of("UTC")).toInstant();
        itemRequest = new ItemRequest(1L, "request desc", user, new ArrayList<>(), created);
    }

    @Nested
    class CreateItemRequest {

        @Test
        void givenNoDesc_whenCreate_gotValidationException() throws Exception {

            CreateItemRequestRequest request = new CreateItemRequestRequest(null);

            mvc.perform(post("/requests")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", user.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEmptyDesc_whenCreate_gotValidationException() throws Exception {

            CreateItemRequestRequest request = new CreateItemRequestRequest("");

            mvc.perform(post("/requests")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", user.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenCreate_gotCreated() throws Exception {
            CreateItemRequestRequest request = new CreateItemRequestRequest("some desc");
            ItemRequest newItemRequest = new ItemRequest(100L, request.getDescription(), user, new ArrayList<>(), created);
            Mockito
                    .when(itemRequestService.createItemRequest(request, user.getId()))
                    .thenReturn(newItemRequest);

            mvc.perform(post("/requests")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", user.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(newItemRequest.getId()), Long.class))
                    .andExpect(jsonPath("$.description", equalTo(newItemRequest.getDescription())))
                    .andExpect(jsonPath("$.created", equalTo("2025-01-01T00:00:00Z")));
        }
    }

    @Test
    void givenUser_whenGetRequests_gotThem() throws Exception {

        ItemRequest req1 = new ItemRequest(100L, "red100", user, new ArrayList<>(), created);
        ItemRequest req2 = new ItemRequest(101L, "red100", user, new ArrayList<>(), created);

        Item item11 = new Item(1L, owner, "item11", "some item11", true, List.of());
        Item item12 = new Item(2L, owner, "item12", "some item12", true, List.of());
        Item item21 = new Item(3L, owner, "item21", "some item21", true, List.of());

        req1.getItems().add(item11);
        req1.getItems().add(item12);
        req2.getItems().add(item21);

        Mockito
                .when(itemRequestService.getUserRequests(user.getId()))
                .thenReturn(List.of(req1, req2));

        mvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(req1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", equalTo(req1.getDescription())))
                .andExpect(jsonPath("$[0].created", equalTo(created.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].id", equalTo(item11.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", equalTo(item11.getName())))
                .andExpect(jsonPath("$[0].items[0].ownerId", equalTo(item11.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$[0].items[1].id", equalTo(item12.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[1].name", equalTo(item12.getName())))
                .andExpect(jsonPath("$[0].items[1].ownerId", equalTo(item12.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$[1].items[0].id", equalTo(item21.getId()), Long.class))
                .andExpect(jsonPath("$[1].items[0].name", equalTo(item21.getName())))
                .andExpect(jsonPath("$[1].items[0].ownerId", equalTo(item21.getOwner().getId()), Long.class));
    }

    @Test
    void givenUser_whenGetRequestsFromOthers_gotThem() throws Exception {

        ItemRequest req1 = new ItemRequest(100L, "red100", owner, new ArrayList<>(), created);
        ItemRequest req2 = new ItemRequest(101L, "red100", owner, new ArrayList<>(), created);

        Mockito
                .when(itemRequestService.getAllRequestsFromOthers(user.getId()))
                .thenReturn(List.of(req1, req2));

        mvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(req1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", equalTo(req1.getDescription())))
                .andExpect(jsonPath("$[0].created", equalTo(created.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(0)))
                .andExpect(jsonPath("$[1].id", equalTo(req2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", equalTo(req2.getDescription())))
                .andExpect(jsonPath("$[1].created", equalTo(created.toString())))
                .andExpect(jsonPath("$[1].items", hasSize(0)));
    }

    @Test
    void givenRequest_whenGet_gotIt() throws Exception {

        ItemRequest request = new ItemRequest(100L, "some request", user, new ArrayList<>(), created);
        Item item1 = new Item(100L, owner, "item1", "some item1", true, List.of());
        Item item2 = new Item(101L, owner, "item2", "some item2", true, List.of());
        request.getItems().add(item1);
        request.getItems().add(item2);

        Mockito
                .when(itemRequestService.getByIdWithItems(request.getId()))
                .thenReturn(request);

        mvc.perform(get("/requests/" + request.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(request.getId()), Long.class))
                .andExpect(jsonPath("$.description", equalTo(request.getDescription())))
                .andExpect(jsonPath("$.created", equalTo(created.toString())))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id", equalTo(item1.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name", equalTo(item1.getName())))
                .andExpect(jsonPath("$.items[0].ownerId", equalTo(item1.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.items[1].id", equalTo(item2.getId()), Long.class))
                .andExpect(jsonPath("$.items[1].name", equalTo(item2.getName())))
                .andExpect(jsonPath("$.items[1].ownerId", equalTo(item2.getOwner().getId()), Long.class));
    }
}