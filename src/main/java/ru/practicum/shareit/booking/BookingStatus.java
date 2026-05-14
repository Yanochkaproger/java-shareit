package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING,   // ожидает одобрения
    APPROVED,  // подтверждено владельцем
    REJECTED,  // отклонено владельцем
    CANCELED
}
