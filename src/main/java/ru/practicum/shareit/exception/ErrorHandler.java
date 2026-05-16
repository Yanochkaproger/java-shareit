package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    // Обработка бизнес-исключений (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (message != null) {
            if (message.contains("уже зарегистрирован") || message.contains("already exists")) {
                status = HttpStatus.CONFLICT; // 409
            } else if (message.contains("не найден") || message.contains("not found")) {
                status = HttpStatus.NOT_FOUND; // 404
            } else if (message.contains("Только владелец") ||
                    message.contains("Доступ запрещён") ||
                    message.contains("access denied")) {
                status = HttpStatus.FORBIDDEN; // 403
            } else if (message.contains("недоступна") ||
                    message.contains("некорректные даты") ||
                    message.contains("арендатор") ||
                    message.contains("must not be blank")) {
                status = HttpStatus.BAD_REQUEST; // 400
            }
        }
        return ResponseEntity.status(status).body(Map.of("error", message != null ? message : "Internal server error"));
    }


    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        String field = e.getFieldError() != null ? e.getFieldError().getField() : "unknown";
        String message = e.getFieldError() != null ? e.getFieldError().getDefaultMessage() : "Validation failed";
        return Map.of("error", field + ": " + message);
    }

    // Ошибки преобразования типов
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Map.of("error", "Неверный формат параметра: " + e.getName());
    }

    // Все остальные исключения
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneric(Exception e) {
        return Map.of("error", "Произошла непредвиденная ошибка: " + e.getMessage());
    }
}

