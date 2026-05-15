package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handle(RuntimeException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (message != null) {
            if (message.contains("Email уже зарегистрирован")) {
                status = HttpStatus.CONFLICT;
            } else if (message.contains("не найден")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("Только владелец")) {
                status = HttpStatus.FORBIDDEN;
            }
        }

        return ResponseEntity.status(status)
                .body(Map.of("error", message != null ? message : "Internal server error"));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String field = e.getFieldError() != null ? e.getFieldError().getField() : "unknown";
        String message = e.getFieldError() != null ? e.getFieldError().getDefaultMessage() : "Validation failed";
        return Map.of("error", field + ": " + message);
    }
}
