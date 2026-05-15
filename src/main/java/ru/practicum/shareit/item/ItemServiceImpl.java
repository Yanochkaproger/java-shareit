package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {

        if (!userRepository.findById(ownerId).isPresent()) {
            throw new RuntimeException("Пользователь не найден");
        }

        Item item = itemMapper.toItem(dto);
        item.setOwnerId(ownerId);
        Item saved = itemRepository.save(item);
        return itemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {

        if (!userRepository.findById(ownerId).isPresent()) {
            throw new RuntimeException("Пользователь не найден");
        }

        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));


        if (!existing.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Только владелец может редактировать вещь");
        }


        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existing.setAvailable(dto.getAvailable());
        }

        Item updated = itemRepository.update(existing);
        return itemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {

        if (!userRepository.findById(ownerId).isPresent()) {
            throw new RuntimeException("Пользователь не найден");
        }

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {

        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
