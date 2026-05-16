package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.user.User;
import java.util.List;

public final class ItemMapper {

    private ItemMapper() {
    }


    public static Item toItem(ItemDto dto, User owner) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwner(owner);
        item.setRequestId(dto.getRequestId());
        return item;
    }

    public static ItemDto toItemDto(Item item, List<CommentDto> comments) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setOwnerId(item.getOwner() != null ? item.getOwner().getId() : null);
        dto.setRequestId(item.getRequestId());
        dto.setComments(comments);
        return dto;
    }

    public static ItemDto toItemDtoWithBookings(Item item, List<CommentDto> comments, Booking last, Booking next) {
        ItemDto dto = toItemDto(item, comments);
        dto.setLastBooking(last != null ? toBookingShortDto(last) : null);
        dto.setNextBooking(next != null ? toBookingShortDto(next) : null);
        return dto;
    }

    private static BookingShortDto toBookingShortDto(Booking b) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(b.getId());
        dto.setStart(b.getStart());
        dto.setEnd(b.getEnd());
        return dto;
    }
}

