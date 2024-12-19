package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dao.UserRepo;
import ru.practicum.shareit.user.dao.UserRepoImpl;
import ru.practicum.shareit.util.TestUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class UserRepoTest {

    private UserRepo repo;

    @BeforeEach
    void setup() {
        repo = new UserRepoImpl();
    }

    @Test
    void givenUser_whenSaved_gotSaved() {
        User user = createAndSaveUser();
        User actUser = getUserById(user.getId());

        assertNotNull(actUser.getId());
        assertEquals(user.getId(), actUser.getId());
        assertEquals(user.getName(), actUser.getName());
        assertEquals(user.getEmail(), actUser.getEmail());
    }

    @Test
    void givenUser_whenGetByEmail_gotUser() {
        User user = createAndSaveUser();

        User actUser = getUserByEmail(user.getEmail());

        assertEquals(user.getId(), actUser.getId());
    }

    @Test
    void givenItem_whenDelete_gotDeleted() {
        User user = createAndSaveUser();

        repo.deleteById(user.getId());

        Optional<User> actUser = repo.getById(user.getId());

        assertTrue(actUser.isEmpty());
    }

    private User createAndSaveUser() {
        User user = TestUtil.getUser();
        return repo.save(user);
    }

    private User getUserById(long userId) {
        return repo.getById(userId).orElseThrow();
    }

    private User getUserByEmail(String email) {
        return repo.getByEmail(email).orElseThrow();
    }
}
