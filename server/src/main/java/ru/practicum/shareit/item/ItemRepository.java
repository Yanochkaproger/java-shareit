package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = {"comments", "comments.author", "owner"})
    List<Item> findByOwnerId(long userId);

    default Item findByIdOrThrow(long itemId) {
        return findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item with %d not found", itemId)));
    }

    @EntityGraph(attributePaths = {"comments", "comments.author", "owner"})
    Optional<Item> findById(long id);

    @Query("SELECT i FROM Item i WHERE " +
            "i.available = true AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> searchAvailableByText(@Param("text") String text);
}
