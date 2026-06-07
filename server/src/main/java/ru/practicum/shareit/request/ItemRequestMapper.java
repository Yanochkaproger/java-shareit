package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.ShortItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        List<ShortItemDto> shortItemDtos = itemRequest.getItems().stream().map(ItemMapper::toShortDto).toList();

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        itemRequestDto.setItems(shortItemDtos);
        return itemRequestDto;
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }
}

