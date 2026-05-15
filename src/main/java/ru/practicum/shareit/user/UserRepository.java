package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Проверка уникальности email
    boolean existsByEmail(String email);

    // Поиск по email (для обновлений)
    Optional<User> findByEmail(String email);
}

