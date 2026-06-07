package ru.practicum.shareit.user;

import ru.practicum.shareit.user.UserDto;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, Long userId);

    void deleteUser(Long userId);

    UserDto getUser(Long userId);
}
