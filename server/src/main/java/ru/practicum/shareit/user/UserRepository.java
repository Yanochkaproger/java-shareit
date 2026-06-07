package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    default User findByIdOrThrow(long userId) {
        return findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with %d not found", userId)));
    }

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, long userId);

}
