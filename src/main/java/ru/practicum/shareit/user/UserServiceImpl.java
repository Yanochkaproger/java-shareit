package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto dto) {
        // ✅ Проверка уникальности email (регистронезависимая)
        if (userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(dto.getEmail()))) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        User user = userMapper.toUser(dto);
        User saved = userRepository.save(user);
        return userMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // ✅ Обновление имени (если передано)
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }

        // ✅ Обновление email с проверкой уникальности
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.findAll().stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(dto.getEmail())
                            && !u.getId().equals(id))) {
                throw new RuntimeException("Email уже зарегистрирован");
            }
            existing.setEmail(dto.getEmail());
        }

        User updated = userRepository.update(existing);
        return userMapper.toUserDto(updated);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }
}

