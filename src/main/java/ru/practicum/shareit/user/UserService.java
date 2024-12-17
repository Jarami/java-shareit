package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepo;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repo;

    public User createUser(@Valid CreateUserRequest request) {

        checkEmail(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        return repo.save(user);
    }

    public User updateUser(@Valid UpdateUserRequest request, long userId) {

        User user = getById(userId);

        String newName = request.getName();
        String newEmail = request.getEmail();

        if (newEmail != null) {
            // проверяем, что с данной почтой не зарегистрирован другой пользователь
            repo.getByEmail(newEmail)
                    .ifPresentOrElse(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new ConflictException("пользователь с почтой %s уже зарегистрирован", newEmail);
                        }
                    },
                    () -> user.setEmail(newEmail));
        }

        if (newName != null && !newName.equals(user.getName())) {
            user.setName(newName);
        }

        return repo.save(user);
    }

    public User getById(long userId) {
        return repo.getById(userId)
                .orElseThrow(() -> new NotFoundException("не найден пользователь с id = %s", userId));
    }

    public User getByEmail(String email) {
        return repo.getByEmail(email)
                .orElseThrow(() -> new NotFoundException("не найден пользователь с email = %s", email));
    }

    public void deleteUserById(long userId) {
        repo.deleteById(userId);
    }

    private void checkEmail(String email) {
        Optional<User> existingUser = repo.getByEmail(email);
        if (existingUser.isPresent()) {
            throw new ConflictException("пользователь с почтой %s уже зарегистрирован", email);
        }
    }
}
