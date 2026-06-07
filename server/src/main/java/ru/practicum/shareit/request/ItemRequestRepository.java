package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.exception.ItemRequestNotFound;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    default ItemRequest findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new ItemRequestNotFound(String.format("Item with %d not found", id)));
    }

    @EntityGraph(attributePaths = {"items", "items.owner"})
    List<ItemRequest> findByUserId(long userId);

    @EntityGraph(attributePaths = {"items", "items.owner"})
    List<ItemRequest> findByUserIdNot(long userId);
}


