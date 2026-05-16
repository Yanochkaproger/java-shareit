package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // Поиск вещей по владельцу (использует связь @ManyToOne)
    @Query("SELECT i FROM Item i WHERE i.owner.id = :ownerId")
    List<Item> findByOwnerId(@Param("ownerId") Long ownerId);

    // Поиск по тексту (только доступные вещи фильтруются в сервисе)
    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Item> searchByNameOrDescription(@Param("text") String text);
}
