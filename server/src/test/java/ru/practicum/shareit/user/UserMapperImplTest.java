package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperImplTest {

    UserMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new UserMapperImpl();
    }

    @Test
    void givenNullUser_whenToDto_gotNullDto() {
        User user = null;
        UserDto dto = mapper.toDto(user);
        assertNull(dto);
    }

    @Test
    void givenValidUser_whenToUser_gotValidDto() {
        User user = new User(1L, "user", "user@mail.ru");
        UserDto dto = mapper.toDto(user);
        assertEquals(1L, user.getId());
        assertEquals("user", user.getName());
        assertEquals("user@mail.ru", user.getEmail());
    }

    @Test
    void givenNullRequest_whenToUser_gotNullUser() {
        CreateUserRequest request = null;
        User user = mapper.toUser(request);
        assertNull(user);
    }

    @Test
    void givenValidRequest_whenToUser_gotValidUser() {
        CreateUserRequest request = new CreateUserRequest("user", "user@mail.ru");
        User user = mapper.toUser(request);
        assertNull(user.getId());
        assertEquals("user", user.getName());
        assertEquals("user@mail.ru", user.getEmail());
    }
}