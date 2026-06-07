package ru.practicum.shareit.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    private HttpStatus status;
    private String error;

    public ErrorResponse(HttpStatus status, String error) {
        this.status = status;
        this.error = error;
    }
}

