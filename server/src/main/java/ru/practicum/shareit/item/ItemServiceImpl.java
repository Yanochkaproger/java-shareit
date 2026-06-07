package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ResponseItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestService itemRequestService;

    @Override
    @Transactional
    public ResponseItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = userRepository.findByIdOrThrow(userId);

        Item item = itemRepository.save(ItemMapper.toEntity(itemDto, owner));
        if (itemDto.getRequestId() != null) {
            itemRequestService.addItemToRequest(itemDto.getRequestId(), item);
        }

        return ItemMapper.toDto(item);
    }

    @Override
    @Transactional
    public ResponseItemDto updateItem(ItemDto itemDto, Long itemId, Long userId) {
        User owner = userRepository.findByIdOrThrow(userId);
        Item currentItem = itemRepository.findByIdOrThrow(itemId);

        if (!currentItem.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the owner can edit item");
        }

        if (itemDto.getName() != null) {
            currentItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            currentItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            currentItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toDto(currentItem);
    }

    @Override
    public ResponseItemDto getItemById(Long itemId) {
        Item currentItem = itemRepository.findById(itemId)
                .orElseThrow(
                        () -> new ItemNotFoundException(String.format("Item with %d not found", itemId))
                );

        return ItemMapper.toDto(currentItem);
    }

    @Override
    public List<ResponseItemDto> getOwnerItems(Long userId) {
        userRepository.findByIdOrThrow(userId);

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, Booking> lastBookings = bookingRepository.findLastItemsBooking(itemIds)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        Function.identity()
                ));
        Map<Long, Booking> nextBookings = bookingRepository.findNextItemsBooking(itemIds)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        Function.identity()
                ));

        return items.stream()
                .map(item -> ItemMapper.toOwnerDto(
                        item,
                        lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()))
                )
                .toList();
    }

    @Override
    public List<ResponseItemDto> searchAvailableItems(String text) {
        List<Item> items = itemRepository.searchAvailableByText(text);

        return items.stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto dto) {
        Item item = itemRepository.findByIdOrThrow(itemId);
        User author = userRepository.findByIdOrThrow(userId);

        if (!bookingRepository.hasUserCompletedBooking(userId, itemId)) {
            throw new ValidationException("Comment available only for renters");
        }

        Comment newComment = CommentMapper.toEntity(dto, item, author);
        return CommentMapper.toDto(commentRepository.save(newComment));
    }
}
