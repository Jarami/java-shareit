package ru.practicum.shareit.user;

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

    @Nested
    class CreateUser {

        @Test
        void givenNoName_whenCreateUser_gotValidationException() throws Exception {
            CreateUserRequest request = new CreateUserRequest(null, "someUser@mail.ru");

            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenEmptyName_whenCreateUser_gotValidationException() throws Exception {
            CreateUserRequest request = new CreateUserRequest("", "someUser@mail.ru");

            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenNoEmail_whenCreateUser_gotValidationException() throws Exception {
            CreateUserRequest request = new CreateUserRequest("some user", null);

            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenInvalidEmail_whenCreateUser_gotValidationException() throws Exception {
            CreateUserRequest request = new CreateUserRequest("some user", "@mail.ru");

            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenCreateUser_gotCreated() throws Exception {

            CreateUserRequest request = new CreateUserRequest("some user", "someUser@mail.ru");

            User newUser = new User(100L, request.getName(), request.getEmail());

            Mockito
                    .when(userService.createUser(request))
                    .thenReturn(newUser);

            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", equalTo(newUser.getId()), Long.class))
                    .andExpect(jsonPath("$.name", equalTo(newUser.getName())))
                    .andExpect(jsonPath("$.email", equalTo(newUser.getEmail())));
        }
    }

    @Nested
    class UpdatedUser {

        @Test
        void givenEmptyName_whenUpdate_gotValidationException() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest("", "newUser@mail.ru");

            mvc.perform(patch("/users/" + user.getId())
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenInvalidDesc_whenUpdate_gotValidationException() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest("new user", "@mail.ru");

            mvc.perform(patch("/users/" + user.getId())
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        void givenValidRequest_whenUpdate_gotUpdated() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest("new user", "newUser@mail.ru");
            User updatedUser = new User(user.getId(), request.getName(), request.getEmail());

            Mockito
                    .when(userService.updateUser(request, user.getId()))
                    .thenReturn(updatedUser);

            mvc.perform(patch("/users/" + user.getId())
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", equalTo(user.getId()), Long.class))
                    .andExpect(jsonPath("$.name", equalTo(updatedUser.getName())))
                    .andExpect(jsonPath("$.email", equalTo(updatedUser.getEmail())));
        }
    }

    @Test
    void getUserById() throws Exception {
        Mockito
                .when(userService.getById(user.getId()))
                .thenReturn(user);

        mvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", equalTo(user.getName())))
                .andExpect(jsonPath("$.email", equalTo(user.getEmail())));
    }

    @Test
    void deleteById() throws Exception {

        mvc.perform(delete("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1))
                .deleteUserById(user.getId());
    }
}