package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class ItemDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}

