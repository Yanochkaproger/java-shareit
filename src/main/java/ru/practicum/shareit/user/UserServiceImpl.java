package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email '" + dto.getEmail() + "' уже зарегистрирован");
        }
        User user = UserMapper.toUser(dto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id=" + id + " не найден"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' уже зарегистрирован");
            }
            existing.setEmail(dto.getEmail());
        }
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        return UserMapper.toUserDto(userRepository.save(existing));
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id=" + id + " не найден")));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь с id=" + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}
