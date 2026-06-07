package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.item.ShortItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ShortItemDto> items;
}


