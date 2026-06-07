package ru.practicum.shareit.item;

import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ResponseItemDto;

import java.util.List;

public interface ItemService {

    ResponseItemDto createItem(ItemDto itemDto, Long userId);

    ResponseItemDto updateItem(ItemDto itemDto, Long itemId, Long userId);

    ResponseItemDto getItemById(Long itemId);

    List<ResponseItemDto> getOwnerItems(Long userId);

    List<ResponseItemDto> searchAvailableItems(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto dto);
}

