package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepoImpl implements UserRepo {

    private final Map<Long, User> users = new HashMap<>();
    private long counter = 0;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            long id = generateId();
            user.setId(id);
            users.put(id, user);
        } else {
            users.put(user.getId(), user);
        }
        return user;
    }

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public void deleteById(long id) {
        users.remove(id);
    }

    private long generateId() {
        return counter++;
    }
}
