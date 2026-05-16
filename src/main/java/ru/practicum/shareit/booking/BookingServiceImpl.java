package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookerShortDto;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long bookerId, BookingCreateDto dto) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new RuntimeException(
                        "Пользователь с id=" + bookerId + " не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException(
                        "Вещь с id=" + dto.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new RuntimeException(
                    "Вещь с id=" + dto.getItemId() + " недоступна для бронирования");
        }
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new RuntimeException(
                    "Дата окончания должна быть позже даты начала для бронирования вещи id=" + dto.getItemId());
        }
        if (dto.getStart().isBefore(LocalDateTime.now()) || dto.getEnd().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Даты бронирования не могут быть в прошлом для вещи id=" + dto.getItemId());
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        return toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с id=" + bookingId + " не найдено"));

        Item item = booking.getItem();
        if (item == null) {
            item = itemRepository.findById(booking.getItem().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Вещь для бронирования id=" + bookingId + " не найдена"));
        }

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException(
                    "Только владелец вещи id=" + item.getId() + " может управлять бронированием id=" + bookingId);
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new RuntimeException(
                    "Можно изменить статус только ожидающего бронирования id=" + bookingId +
                            ", текущий статус: " + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с id=" + bookingId + " не найдено"));

        Item item = booking.getItem();
        if (item == null) {
            item = itemRepository.findById(booking.getItem().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Вещь для бронирования id=" + bookingId + " не найдена"));
        }

        if (!booking.getBooker().getId().equals(userId) &&
                !item.getOwner().getId().equals(userId)) {
            throw new RuntimeException(
                    "Доступ к бронированию id=" + bookingId + " запрещён для пользователя id=" + userId);
        }
        return toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long bookerId, String state) {
        if (!userRepository.existsById(bookerId)) {
            throw new RuntimeException("Пользователь с id=" + bookerId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(bookerId);
            case "CURRENT" -> bookingRepository.findCurrentByBookerId(bookerId, now);
            case "PAST" -> bookingRepository.findPastByBookerId(bookerId, now);
            case "FUTURE" -> bookingRepository.findFutureByBookerId(bookerId, now);
            case "WAITING" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
            default -> throw new RuntimeException("Неизвестный статус бронирования: " + state);
        };

        return bookings.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, String state) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь с id=" + ownerId + " не найден");
        }

        List<Booking> bookings = bookingRepository.findByOwnerItems(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> filtered = switch (state.toUpperCase()) {
            case "ALL" -> bookings;
            case "CURRENT" -> bookings.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .collect(Collectors.toList());
            case "PAST" -> bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .sorted((a, b) -> b.getEnd().compareTo(a.getEnd()))
                    .collect(Collectors.toList());
            case "FUTURE" -> bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                    .collect(Collectors.toList());
            case "WAITING" -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.WAITING)
                    .collect(Collectors.toList());
            case "REJECTED" -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                    .collect(Collectors.toList());
            default -> throw new RuntimeException("Неизвестный статус бронирования: " + state);
        };

        return filtered.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Маппинг Entity -> DTO с вложенными объектами
    private BookingDto toDto(Booking booking) {
        BookerShortDto bookerDto = new BookerShortDto(
                booking.getBooker().getId(),
                booking.getBooker().getName());

        ItemShortDto itemDto = new ItemShortDto(
                booking.getItem().getId(),
                booking.getItem().getName());

        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                bookerDto,
                itemDto);
    }
}

