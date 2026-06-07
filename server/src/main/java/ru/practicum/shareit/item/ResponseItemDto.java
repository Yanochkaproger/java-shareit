package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.InfoBookingDto;

import java.util.List;

@Data
public class ResponseItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private InfoBookingDto nextBooking;
    private InfoBookingDto lastBooking;
    private List<CommentDto> comments;
}

