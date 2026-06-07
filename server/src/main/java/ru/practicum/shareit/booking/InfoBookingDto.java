package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InfoBookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
}
