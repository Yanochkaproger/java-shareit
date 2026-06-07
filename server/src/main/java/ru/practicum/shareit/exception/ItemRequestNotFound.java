package ru.practicum.shareit.exception;

public class ItemRequestNotFound extends RuntimeException {

    public ItemRequestNotFound(String message) {
        super(message);
    }
}

