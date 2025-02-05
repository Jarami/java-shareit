package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({UserMapperImpl.class})
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    User user;

    @BeforeEach
    void setup() {
        user = new User(1L, "user", "user@mail.ru");
    }

    @Test
    void createUser() throws Exception {

        CreateUserRequest request = new CreateUserRequest("some user", "someUser@");

        Mockito
                .when(userService.createUser(request))
                .thenReturn(new User(2L, "some user", "someUser@mail.ru"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(2L), Long.class))
                .andExpect(jsonPath("$.name", equalTo("some user")))
                .andExpect(jsonPath("$.email", equalTo("someUser@mail.ru")));
    }

    @Test
    void updateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("new user", "newUser@mail.ru");

        Mockito
                .when(userService.updateUser(request, 1L))
                .thenReturn(new User(1L, "new user", "newUser@mail.ru"));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.name", equalTo("new user")))
                .andExpect(jsonPath("$.email", equalTo("newUser@mail.ru")));
    }

    @Test
    void getUserById() throws Exception {
        Mockito
                .when(userService.getById(1L))
                .thenReturn(user);

        mvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class))
                .andExpect(jsonPath("$.name", equalTo(user.getName())))
                .andExpect(jsonPath("$.email", equalTo(user.getEmail())));
    }

    @Test
    void deleteById() throws Exception {

        mvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1))
                .deleteUserById(user.getId());
    }
}