package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {

    private final Map<Long, Item> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        store.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Item> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Item> findByOwnerId(Long ownerId) {
        return store.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    // 🔍 Поиск: только доступные вещи, по названию ИЛИ описанию
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lowerText = text.toLowerCase();
        return store.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(lowerText) ||
                                item.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }

    public Item update(Item item) {
        if (!store.containsKey(item.getId())) {
            return null;
        }
        store.put(item.getId(), item);
        return item;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}

