package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto dto) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        Item item = itemMapper.toItem(dto);
        item.setOwnerId(ownerId);
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));
        if (!existing.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Только владелец может редактировать вещь");
        }
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) existing.setAvailable(dto.getAvailable());
        return itemMapper.toItemDto(itemRepository.save(existing));
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));
        ItemDto dto = itemMapper.toItemDto(item);
        LocalDateTime now = LocalDateTime.now();

        // Комментарии с реальными именами авторов
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(c -> {
                    CommentDto cdto = new CommentDto();
                    cdto.setId(c.getId());
                    cdto.setText(c.getText());
                    cdto.setCreated(c.getCreated());
                    User author = userRepository.findById(c.getAuthorId())
                            .orElseThrow(() -> new RuntimeException("Автор не найден"));
                    cdto.setAuthorName(author.getName());
                    return cdto;
                })
                .collect(Collectors.toList());
        dto.setComments(comments);

        // Last/Next бронирования
        Booking last = bookingRepository.findLastApproved(itemId, now);
        if (last != null) {
            dto.setLastBooking(createShortBookingDto(last));
        }
        Booking next = bookingRepository.findNextApproved(itemId, now);
        if (next != null) {
            dto.setNextBooking(createShortBookingDto(next));
        }
        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toItemDto(item);
                    Booking last = bookingRepository.findLastApproved(item.getId(), now);
                    if (last != null) dto.setLastBooking(createShortBookingDto(last));
                    Booking next = bookingRepository.findNextApproved(item.getId(), now);
                    if (next != null) dto.setNextBooking(createShortBookingDto(next));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.searchByNameOrDescription(text).stream()
                .filter(Item::getAvailable)
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentCreateDto dto) {
        if (!userRepository.existsById(authorId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        // только завершённое подтверждённое бронирование
        boolean hasFinishedBooking = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(authorId, BookingStatus.APPROVED)
                .stream()
                .anyMatch(b -> b.getItemId().equals(itemId) && b.getEnd().isBefore(LocalDateTime.now()));

        if (!hasFinishedBooking) {
            throw new RuntimeException("Только арендатор может оставить отзыв после завершения аренды");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItemId(itemId);
        comment.setAuthorId(authorId);
        comment.setCreated(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        CommentDto result = new CommentDto();
        result.setId(saved.getId());
        result.setText(saved.getText());
        result.setCreated(saved.getCreated());
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));
        result.setAuthorName(author.getName());
        return result;
    }


    private BookingDto createShortBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        // Для ItemDto не нужны вложенные booker/item
        return dto;
    }
}
