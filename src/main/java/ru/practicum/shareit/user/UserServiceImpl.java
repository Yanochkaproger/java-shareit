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
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        // JPA-метод для проверки уникальности (эффективный SQL EXISTS)
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        User user = userMapper.toUser(dto);
        User saved = userRepository.save(user); // INSERT в БД
        return userMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        //  Обновление имени (если передано)
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }

        // Обновление email с проверкой уникальности через JPA
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email уже зарегистрирован");
            }
            existing.setEmail(dto.getEmail());
        }

        User updated = userRepository.save(existing);
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
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(id); // DELETE из БД
    }
}
