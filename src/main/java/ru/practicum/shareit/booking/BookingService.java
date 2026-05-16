package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.List;

public interface BookingService {

    BookingDto create(Long bookerId, BookingCreateDto dto);

    BookingDto updateStatus(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getByBooker(Long bookerId, String state);

    List<BookingDto> getByOwner(Long ownerId, String state);
}

