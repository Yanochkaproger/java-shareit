package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.RequestUserDto;
import ru.practicum.shareit.user.UpdateUserDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid RequestUserDto userDto) {
        log.info("Creating user {}", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping(path = "/{userId}")
    public ResponseEntity<Object> updateUser(
            @RequestBody @Valid UpdateUserDto userDto,
            @PathVariable Long userId) {
        log.info("Updating user {}", userDto);
        return userClient.updateUser(userDto, userId);
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("Deleting user by id {}", userId);
        return userClient.deleteUser(userId);
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable Long userId) {
        log.info("Get user by id {}", userId);
        return userClient.getUser(userId);
    }
}
