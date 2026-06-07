package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestCommentDto {
    @NotBlank
    private String text;
}
