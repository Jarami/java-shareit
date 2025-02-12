package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.shareit.user.CreateUserRequest;
import ru.practicum.shareit.user.UpdateUserRequest;
import ru.practicum.shareit.user.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@Testcontainers
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShareItAppTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14")
            .withUsername("user")
            .withPassword("pass")
            .withReuse(true)
            .withDatabaseName("shareit");

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    class UserTests {

        @BeforeEach
        void setup() {
            restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        }

        @Test
        void testCreateUser() {
            ResponseEntity<UserDto> response = createUserEntity("user1", "user1@mail.ru");

            assertEquals(HttpStatusCode.valueOf(201), response.getStatusCode());
            assertEquals("user1", response.getBody().getName());
            assertEquals("user1@mail.ru", response.getBody().getEmail());
        }

        @Test
        void testUpdateUser() {
            UserDto user = createUser("user2", "user2@mail.ru");
            ResponseEntity<UserDto> response = updateUserEntity("newUser2", "newUser2@mail.ru", user.getId());

            assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
            assertEquals("newUser2", response.getBody().getName());
            assertEquals("newUser2@mail.ru", response.getBody().getEmail());
        }

        @Test
        void testGetUserById() {
            UserDto user = createUser("user3", "user3@mail.ru");
            UserDto actualUser = getUserById(user.getId());
            assertEquals(user.getId(), actualUser.getId());
            assertEquals("user3@mail.ru", actualUser.getEmail());
        }
    }

    private ResponseEntity<UserDto> createUserEntity(String name, String email) {
        CreateUserRequest request = new CreateUserRequest(name, email);
        return restTemplate.postForEntity("/users", request, UserDto.class);
    }

    private UserDto createUser(String name, String email) {
        return createUserEntity(name, email).getBody();
    }

    private ResponseEntity<UserDto> updateUserEntity(String newName, String newEmail, long userId) {
        HttpEntity<UpdateUserRequest> request = new HttpEntity<>(new UpdateUserRequest(newName, newEmail));
        return restTemplate.exchange("/users/" + userId, HttpMethod.PATCH, request, UserDto.class);
    }

    private UserDto getUserById(long userId) {
        ResponseEntity<UserDto> response = restTemplate.getForEntity("/users/" + userId, UserDto.class);
        return response.hasBody() ? response.getBody() : null;
    }

    private void deleteUserById(long userId) {
        restTemplate.delete("/users/" + userId);
    }
}
