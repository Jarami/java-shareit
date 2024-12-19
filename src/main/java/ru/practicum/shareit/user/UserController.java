package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return new ResponseEntity<>(UserMapper.mapToDto(user), HttpStatus.CREATED);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UpdateUserRequest request, @PathVariable Long userId) {
        User user = userService.updateUser(request, userId);
        return ResponseEntity.ok()
                .body(UserMapper.mapToDto(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return ResponseEntity.ok()
                .body(UserMapper.mapToDto(user));
    }

    @DeleteMapping("/{userId}")
    public void deleteById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
