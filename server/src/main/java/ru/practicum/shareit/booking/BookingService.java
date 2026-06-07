package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.RequestBookingDto;
import ru.practicum.shareit.booking.ResponseBookingDto;
import ru.practicum.shareit.booking.BookingState;

import java.util.List;

public interface BookingService {

    ResponseBookingDto createBooking(RequestBookingDto bookingDto, Long userId);

    ResponseBookingDto resolveBooking(Long userId, Long bookingId, boolean approved);

    ResponseBookingDto getBooking(Long userId, Long bookingId);

    List<ResponseBookingDto> getUserBookings(Long userId, BookingState state);

    List<ResponseBookingDto> getOwnerBookings(Long ownerId, BookingState state);
}

