package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class ShortItemDto {
    private Long id;
    private String name;
    private Long ownerId;
}

