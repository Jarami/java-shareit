package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository, new UserMapperImpl());

        user1 = new User(1L, "user1", "user1@mail.ru");
        user2 = new User(2L, "user2", "user2@mail.ru");
    }

    @Nested
    class CreateUser {
        @Test
        void givenExistingEmail_whenCreateUser_gotConflictException() {
            mockUserByEmail(user1);

            CreateUserRequest request = new CreateUserRequest(user1.getName(), user1.getEmail());

            assertThrows(ConflictException.class, () -> userService.createUser(request));
        }

        @Test
        void givenNonExistingEmail_whenCreateUser_gotCreated() {
            Mockito
                    .when(userRepository.findByEmail("newUser@mail.ru"))
                    .thenReturn(Optional.empty());

            mockUserSave();

            CreateUserRequest request = new CreateUserRequest("newUser", "newUser@mail.ru");

            User user = userService.createUser(request);
            assertEquals(1000L, user.getId());
            assertEquals("newUser", user.getName());
            assertEquals("newUser@mail.ru", user.getEmail());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void givenExistingEmail_whenUpdateItem_gotConflictException() {

            mockUserById(user1);
            mockUserByEmail(user2);

            UpdateUserRequest request = new UpdateUserRequest("new user1", "user2@mail.ru");

            assertThrows(ConflictException.class, () ->
                    userService.updateUser(request, user1.getId()));
        }

        @Test
        void givenNewName_whenUpdateUser_gotNewName() {

            mockUserById(user1);
            mockUserUpdate(user1);

            UpdateUserRequest request = new UpdateUserRequest("new user1", null);

            userService.updateUser(request, user1.getId());

            assertEquals("new user1", user1.getName());
            assertEquals("user1@mail.ru", user1.getEmail());
        }

        @Test
        void givenNewDesc_whenUpdateItem_gotNewDesc() {

            mockUserById(user1);
            mockUserUpdate(user1);

            UpdateUserRequest request = new UpdateUserRequest(null, "newUser1@mail.ru");

            userService.updateUser(request, user1.getId());

            assertEquals("user1", user1.getName());
            assertEquals("newUser1@mail.ru", user1.getEmail());
        }
    }

    @Nested
    class GetById {

        @Test
        void givenExistingItem_whenGetById_gotIt() {
            mockUserById(user1);

            User actualUser = userService.getById(user1.getId());

            assertEquals(user1, actualUser);
        }

        @Test
        void givenAbsentItem_whenGetById_gotNonFoundException() {

            Mockito
                    .when(userRepository.findById(1000L))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    userService.getById(1000L));
        }
    }

    @Nested
    class GetByEmail {
        @Test
        void givenExistingUser_whenGetByEmail_gotIt() {
            mockUserByEmail(user1);

            User actualUser = userService.getByEmail(user1.getEmail());

            assertEquals(user1, actualUser);
        }

        @Test
        void givenAbsentItem_whenGetById_gotNonFoundException() {

            Mockito
                    .when(userRepository.findByEmail("no@mail.ru"))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    userService.getByEmail("no@mail.ru"));
        }
    }

    @Test
    void deleteUserById() {
    }

    void mockUserById(User user) {
        Mockito
                .when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
    }

    void mockUserByEmail(User user) {
        Mockito
                .when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    private void mockUserSave() {
        Mockito
                .when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0, User.class);
                    user.setId(1000L);
                    return user;
                });
    }

    private void mockUserUpdate(User user) {
        Mockito
                .when(userRepository.save(user))
                .thenReturn(user);
    }
}