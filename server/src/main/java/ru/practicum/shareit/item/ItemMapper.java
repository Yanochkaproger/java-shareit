package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ResponseItemDto;
import ru.practicum.shareit.item.ShortItemDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static Item toEntity(ItemDto itemDto, User user) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(user);
        return item;
    }

    public static ResponseItemDto toDto(Item item) {
        ResponseItemDto responseItemDto = new ResponseItemDto();
        responseItemDto.setId(item.getId());
        responseItemDto.setName(item.getName());
        responseItemDto.setDescription(item.getDescription());
        responseItemDto.setAvailable(item.getAvailable());
        responseItemDto.setComments(CommentMapper.toDtos(item.getComments()));
        return responseItemDto;
    }

    public static ResponseItemDto toOwnerDto(Item item, Booking last, Booking next) {
        ResponseItemDto responseItemDto = toDto(item);

        responseItemDto.setLastBooking(last != null ? BookingMapper.toInfoDto(last) : null);
        responseItemDto.setNextBooking(next != null ? BookingMapper.toInfoDto(next) : null);

        return responseItemDto;
    }

    public static ShortItemDto toShortDto(Item item) {
        ShortItemDto shortItemDto = new ShortItemDto();
        shortItemDto.setId(item.getId());
        shortItemDto.setName(item.getName());
        shortItemDto.setOwnerId(item.getOwner().getId());
        return shortItemDto;
    }
}
