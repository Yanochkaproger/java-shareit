package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.RequestBookingDto;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.ResponseBookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ResponseBookingDto> createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody RequestBookingDto bookingDto
    ) {
        ResponseBookingDto responseBookingDto = bookingService.createBooking(bookingDto, userId);
        return ResponseEntity.ok(responseBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<ResponseBookingDto> resolveBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved
    ) {
        ResponseBookingDto resolvedBooking = bookingService.resolveBooking(userId, bookingId, approved);
        return ResponseEntity.ok(resolvedBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ResponseBookingDto> getBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId
    ) {
        ResponseBookingDto foundBooking = bookingService.getBooking(userId, bookingId);
        return ResponseEntity.ok(foundBooking);
    }

    @GetMapping
    public ResponseEntity<List<ResponseBookingDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(required = false) BookingState state
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<ResponseBookingDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(required = false) BookingState state
    ) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(ownerId, state));
    }
}
