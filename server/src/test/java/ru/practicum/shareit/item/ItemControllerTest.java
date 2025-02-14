package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({ItemMapperImpl.class})
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CommentService commentService;

    Item item;
    User owner;
    User user;

    @BeforeEach
    void setup() {
        owner = new User(1L, "owner", "owner@mail.ru");
        user = new User(2L, "user", "user@mail.ru");
        item = new Item(1L, owner, "item", "some item", true, new ArrayList<>());
    }

    @Nested
    class CreateItem {

        @Test
        void givenNoName_whenCreate_gotValidationException() throws Exception {
            CreateItemRequest request = new CreateItemRequest(null, item.getDescription(), item.isAvailable(), null);

            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEmptyName_whenCreate_gotValidationException() throws Exception {
            CreateItemRequest request = new CreateItemRequest("", item.getDescription(), item.isAvailable(), null);

            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoDesc_whenCreate_gotValidationException() throws Exception {
            CreateItemRequest request = new CreateItemRequest(item.getName(), null, item.isAvailable(), null);

            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEmptyDesc_whenCreate_gotValidationException() throws Exception {
            CreateItemRequest request = new CreateItemRequest(item.getName(), "", item.isAvailable(), null);

            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoAvailable_whenCreate_gotValidationException() throws Exception {
            CreateItemRequest request = new CreateItemRequest(item.getName(), item.getDescription(), null, null);

            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenCreate_gotCreated() throws Exception {

            CreateItemRequest request = new CreateItemRequest(item.getName(), item.getDescription(), item.isAvailable(), null);

            Mockito
                    .when(itemService.createItem(request, owner.getId()))
                    .thenReturn(item);

            mvc.perform(post("/items")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(item.getId()), Long.class))
                    .andExpect(jsonPath("$.name", equalTo(item.getName())))
                    .andExpect(jsonPath("$.description", equalTo(item.getDescription())))
                    .andExpect(jsonPath("$.available", equalTo(item.isAvailable())))
                    .andExpect(jsonPath("$.comments", equalTo(List.of())));
        }
    }

    @Nested
    class UpdateItem {

        @Test
        void givenEmptyName_whenUpdate_gotValidationException() throws Exception {
            UpdateItemRequest request = new UpdateItemRequest("", "some new desc", false);

            mvc.perform(patch("/items/" + item.getId())
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEmptyDesc_whenUpdate_gotValidationException() throws Exception {

            UpdateItemRequest request = new UpdateItemRequest("some new item", "", false);

            mvc.perform(patch("/items/" + item.getId())
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenUpdate_gotUpdated() throws Exception {

            UpdateItemRequest request = new UpdateItemRequest("some new item", "some new desc", false);

            Item newItem = new Item(item.getId(), owner, "some new item", "some new desc", false, new ArrayList<>());

            Mockito
                    .when(itemService.updateItem(request, newItem.getId(), owner.getId()))
                    .thenReturn(newItem);

            mvc.perform(patch("/items/" + item.getId())
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", owner.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(item.getId()), Long.class))
                    .andExpect(jsonPath("$.name", equalTo("some new item")))
                    .andExpect(jsonPath("$.description", equalTo("some new desc")))
                    .andExpect(jsonPath("$.available", equalTo(false)))
                    .andExpect(jsonPath("$.comments", equalTo(List.of())));
        }
    }

    @Test
    void getById() throws Exception {
        Mockito
                .when(itemService.getById(item.getId()))
                .thenReturn(item);

        mvc.perform(get("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", equalTo(item.getName())))
                .andExpect(jsonPath("$.description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$.available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$.comments", equalTo(List.of())));
    }

    @Test
    void getByUserId() throws Exception {

        LocalDateTime now = LocalDateTime.parse("2025-01-01T00:00:00");

        Mockito
                .when(itemService.getByUserId(owner.getId(), now))
                .thenReturn(List.of(item));

        mvc.perform(get("/items?now=2025-01-01T00:00:00")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", equalTo(item.getName())))
                .andExpect(jsonPath("$[0].description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$[0].available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$[0].comments", equalTo(List.of())));
    }

    @Test
    void search() throws Exception {
        Mockito
                .when(itemService.search("abc"))
                .thenReturn(List.of(item));

        mvc.perform(get("/items/search?text=abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", equalTo(item.getName())))
                .andExpect(jsonPath("$[0].description", equalTo(item.getDescription())))
                .andExpect(jsonPath("$[0].available", equalTo(item.isAvailable())))
                .andExpect(jsonPath("$[0].comments", equalTo(List.of())));
    }

    @Nested
    class CreateComment {

        @Test
        void givenNoText_whenCreateComment_gotValidationException() throws Exception {
            CreateCommentRequest request = new CreateCommentRequest(null);

            mvc.perform(post("/items/1/comment?now=2025-01-01T00:00:00")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", user.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEmptyText_whenCreateComment_gotValidationException() throws Exception {
            CreateCommentRequest request = new CreateCommentRequest("");

            mvc.perform(post("/items/1/comment?now=2025-01-01T00:00:00")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", user.getId()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenCreateComment_gotCreated() throws Exception {

            CreateCommentRequest request = new CreateCommentRequest("comment");
            LocalDateTime now = LocalDateTime.parse("2025-01-01T00:00:00");
            Comment comment = new Comment(1L, "comment", user, item, now.atZone(ZoneId.systemDefault()).toInstant());

            Mockito
                    .when(commentService.createComment(request, item.getId(), user.getId(), now))
                    .thenReturn(comment);

            mvc.perform(post("/items/1/comment?now=2025-01-01T00:00:00")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", user.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(comment.getId()), Long.class))
                    .andExpect(jsonPath("$.text", equalTo(request.getText())))
                    .andExpect(jsonPath("$.authorName", equalTo(user.getName())));
        }
    }
}