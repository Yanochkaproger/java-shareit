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
        if (!userRepository.existsById(bookerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new RuntimeException("Вещь недоступна для бронирования");
        }
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new RuntimeException("Дата окончания должна быть позже даты начала");
        }
        if (dto.getStart().isBefore(LocalDateTime.now()) || dto.getEnd().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Даты бронирования не могут быть в прошлом");
        }

        Booking booking = new Booking();
        booking.setItemId(dto.getItemId());
        booking.setBookerId(bookerId);
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Только владелец вещи может управлять бронированиями");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new RuntimeException("Можно изменить статус только ожидающего бронирования");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return mapToDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new RuntimeException("Доступ запрещён");
        }
        return mapToDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long bookerId, String state) {
        if (!userRepository.existsById(bookerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(bookerId);
            case "CURRENT" -> bookingRepository.findCurrentByBookerId(bookerId, now);
            case "PAST" -> bookingRepository.findPastByBookerId(bookerId, now);
            case "FUTURE" -> bookingRepository.findFutureByBookerId(bookerId, now);
            case "WAITING" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
            default -> throw new RuntimeException("Неизвестный статус: " + state);
        };
        return bookings.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, String state) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        List<Booking> all = bookingRepository.findByOwnerItems(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> filtered = switch (state.toUpperCase()) {
            case "ALL" -> all;
            case "CURRENT" -> all.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .collect(Collectors.toList());
            case "PAST" -> all.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .sorted((a, b) -> b.getEnd().compareTo(a.getEnd()))
                    .collect(Collectors.toList());
            case "FUTURE" -> all.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                    .collect(Collectors.toList());
            case "WAITING" -> all.stream()
                    .filter(b -> b.getStatus() == BookingStatus.WAITING)
                    .collect(Collectors.toList());
            case "REJECTED" -> all.stream()
                    .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                    .collect(Collectors.toList());
            default -> throw new RuntimeException("Неизвестный статус: " + state);
        };
        return filtered.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // Маппер с вложенными объектами
    private BookingDto mapToDto(Booking booking) {
        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        BookerShortDto bookerDto = new BookerShortDto();
        bookerDto.setId(booker.getId());
        bookerDto.setName(booker.getName());

        ItemShortDto itemDto = new ItemShortDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        dto.setBooker(bookerDto);
        dto.setItem(itemDto);
        return dto;
    }
}
