package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto dto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toItemDto(itemRepository.save(item), List.of());
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));
        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Только владелец может редактировать вещь");
        }
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());

        return ItemMapper.toItemDto(existing, List.of());
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(c -> {
                    User author = userRepository.findById(c.getAuthorId())
                            .orElseThrow(() -> new RuntimeException("Автор не найден"));
                    return new CommentDto(c.getId(), c.getText(), author.getName(), c.getCreated());
                }).collect(Collectors.toList());

        // Используем метод БЕЗ бронирований
        return ItemMapper.toItemDto(item, comments);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Item> items = itemRepository.findByOwnerId(ownerId);

        return items.stream().map(item -> {
            List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                    .map(c -> {
                        User author = userRepository.findById(c.getAuthorId())
                                .orElseThrow(() -> new RuntimeException("Автор не найден"));
                        return new CommentDto(c.getId(), c.getText(), author.getName(), c.getCreated());
                    }).collect(Collectors.toList());

            Booking last = bookingRepository.findLastByItemId(item.getId(), now).orElse(null);
            Booking next = bookingRepository.findNextByItemId(item.getId(), now).orElse(null);

            //  Используем метод С бронированиями
            return ItemMapper.toItemDtoWithBookings(item, comments, last, next);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.searchByNameOrDescription(text).stream()
                .filter(Item::getAvailable)
                .map(item -> ItemMapper.toItemDto(item, List.of()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentCreateDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        if (!bookingRepository.existsFinishedBooking(authorId, itemId, LocalDateTime.now())) {
            throw new RuntimeException("Только арендатор может оставить отзыв после завершения аренды");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthorId(authorId);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));

        return new CommentDto(saved.getId(), saved.getText(), author.getName(), saved.getCreated());
    }
}
