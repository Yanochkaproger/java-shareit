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
import java.util.Map;
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
                .orElseThrow(() -> new RuntimeException("Пользователь с id=" + ownerId + " не найден"));
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toItemDto(itemRepository.save(item), List.of());
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь с id=" + itemId + " не найдена"));
        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Только владелец вещи с id=" + itemId + " может её редактировать");
        }
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());
        return ItemMapper.toItemDto(existing, List.of());
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь с id=" + itemId + " не найдена"));

        List<CommentDto> comments = loadComments(itemId);
        return ItemMapper.toItemDto(item, comments);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь с id=" + ownerId + " не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();


        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));


        Map<Long, Booking> lastBookings = itemIds.stream()
                .collect(Collectors.toMap(id -> id, id -> bookingRepository.findLastByItemId(id, now).orElse(null)));
        Map<Long, Booking> nextBookings = itemIds.stream()
                .collect(Collectors.toMap(id -> id, id -> bookingRepository.findNextByItemId(id, now).orElse(null)));

        return items.stream().map(item -> {
            List<CommentDto> comments = commentsMap.getOrDefault(item.getId(), List.of()).stream()
                    .map(c -> {
                        User author = userRepository.findById(c.getAuthorId())
                                .orElseThrow(() -> new RuntimeException("Автор с id=" + c.getAuthorId() + " не найден"));
                        return new CommentDto(c.getId(), c.getText(), author.getName(), c.getCreated());
                    }).collect(Collectors.toList());


            return ItemMapper.toItemDtoWithBookings(item, comments, lastBookings.get(item.getId()), nextBookings.get(item.getId()));
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
                .orElseThrow(() -> new RuntimeException("Вещь с id=" + itemId + " не найдена"));

        if (!bookingRepository.existsFinishedBooking(authorId, itemId, LocalDateTime.now())) {
            throw new RuntimeException("Только арендатор может оставить отзыв после завершения аренды вещи id=" + itemId);
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthorId(authorId);
        comment.setCreated(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Пользователь с id=" + authorId + " не найден"));

        return new CommentDto(saved.getId(), saved.getText(), author.getName(), saved.getCreated());
    }

    private List<CommentDto> loadComments(Long itemId) {
        return commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(c -> {
                    User author = userRepository.findById(c.getAuthorId())
                            .orElseThrow(() -> new RuntimeException("Автор с id=" + c.getAuthorId() + " не найден"));
                    return new CommentDto(c.getId(), c.getText(), author.getName(), c.getCreated());
                }).collect(Collectors.toList());
    }
}
