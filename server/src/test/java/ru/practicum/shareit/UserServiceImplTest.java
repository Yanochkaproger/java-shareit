package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class UserServiceImplTest {

    private final UserService userService;

    @Test
    void testUpdateUser() {
        UserDto user = userService.createUser(makeUserDto("abc", "abc@mail.ru"));
        userService.updateUser(makeUserDto(null, "abc1@mail.ru"), user.getId());

        UserDto result = userService.getUser(user.getId());

        assertThat(result.getName(), equalTo("abc"));
        assertThat(result.getEmail(), equalTo("abc1@mail.ru"));
    }

    @Test
    void testDeleteUser() {
        var user = userService.createUser(makeUserDto("abc", "delete@mail.ru"));
        userService.deleteUser(user.getId());

        assertThrows(Exception.class, () -> userService.getUser(user.getId()));
    }

    @Test
    void testCreateUserDuplicateEmail() {
        userService.createUser(makeUserDto("first", "dup@mail.ru"));

        assertThrows(Exception.class, () ->
                userService.createUser(makeUserDto("second", "dup@mail.ru"))
        );
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }
}
