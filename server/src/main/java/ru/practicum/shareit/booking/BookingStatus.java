package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingStatus {
    WAITING,
    APPROVED,
    REJECTED
}

