package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestDto dto, Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.toEntity(dto, user));
        return ItemRequestMapper.toDto(itemRequest);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findByIdOrThrow(userId);

        return ItemRequestMapper.toDto(itemRequestRepository.findByIdOrThrow(requestId));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        return itemRequestRepository.findByUserId(userId).stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getOtherUsersRequests(Long userId) {
        return itemRequestRepository.findByUserIdNot(userId).stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void addItemToRequest(Long requestId, Item item) {
        ItemRequest itemRequest = itemRequestRepository.findByIdOrThrow(requestId);
        itemRequest.getItems().add(item);
    }
}

