package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException e) {
        String message = e.getMessage();


        if (message != null && message.contains("не найден")) {
            return Map.of("error", message);
        }


        if (message != null && message.contains("Email уже зарегистрирован")) {
            return Map.of("error", message);
        }


        if (message != null && message.contains("Только владелец")) {
            return Map.of("error", message);
        }

        return Map.of("error", message != null ? message : "Internal server error");
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

