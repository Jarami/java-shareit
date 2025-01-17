package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final UserMapper mapper;

    public User createUser(@Valid CreateUserRequest request) {
        log.info("creating user {}", request);
        checkEmail(request.getEmail());
        User user = mapper.toUser(request);
        log.info("saving user {}", user);
        return repo.save(user);
    }

    public User updateUser(@Valid UpdateUserRequest request, long userId) {

        log.info("updating user {} as {}", userId, request);

        User user = getById(userId);
        log.info("got user {}", user);

        String newName = request.getName();
        String newEmail = request.getEmail();

        checkUpdatingEmail(userId, newEmail);

        if (newEmail != null) {
            user.setEmail(newEmail);
        }

        if (newName != null) {
            user.setName(newName);
        }

        return repo.save(user);
    }

    public User getById(long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new NotFoundException("не найден пользователь с id = %s", userId));
    }

    public User getByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("не найден пользователь с email = %s", email));
    }

    public void deleteUserById(long userId) {
        repo.deleteById(userId);
    }

    private void checkEmail(String email) {
        Optional<User> existingUser = repo.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new ConflictException("пользователь с почтой %s уже зарегистрирован", email);
        }
    }

    // проверяем, что с данной почтой не зарегистрирован другой пользователь
    private void checkUpdatingEmail(long userId, String email) {
        repo.findByEmail(email)
            .ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new ConflictException("пользователь с почтой %s уже зарегистрирован", email);
                }
            });
    }
}
