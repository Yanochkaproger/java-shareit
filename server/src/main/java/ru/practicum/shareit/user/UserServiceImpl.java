package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailConflictException;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (isExistsAnyUserByEmail(userDto.getEmail())) {
            throw new EmailConflictException(String.format("Email %s is already exists", userDto.getEmail()));
        }

        User user = UserMapper.toEntity(userDto);
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override

    @Transactional
    public UserDto updateUser(UserDto userDto, Long userId) {
        User currentUser = userRepository.findByIdOrThrow(userId);

        if (userDto.getName() != null) {
            currentUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (isExistsAnotherUserByEmail(userDto.getEmail(), currentUser.getId())) {
                throw new EmailConflictException(String.format("Email %s is already taken", userDto.getEmail()));
            }
            currentUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toDto(currentUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUser(Long userId) {
        User currentUser = userRepository.findByIdOrThrow(userId);
        return UserMapper.toDto(currentUser);
    }

    private boolean isExistsAnyUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private boolean isExistsAnotherUserByEmail(String email, Long userId) {
        return userRepository.existsByEmailAndIdNot(email, userId);
    }
}

