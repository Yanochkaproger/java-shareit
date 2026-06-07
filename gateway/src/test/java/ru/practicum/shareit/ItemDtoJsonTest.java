package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> requestJson;

    @Autowired
    private JacksonTester<UpdateItemDto> updateJson;

    @Autowired
    private JacksonTester<RequestCommentDto> commentJson;

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void serializeItemDto() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Ударная дрель");
        dto.setAvailable(true);
        dto.setRequestId(5L);

        JsonContent<ItemDto> result = requestJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ударная дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void deserializeItemDto() throws Exception {
        String content = "{\"name\":\"Отвертка\",\"description\":\"Крестовая отвертка\",\"available\":false}";

        ItemDto dto = requestJson.parse(content).getObject();

        assertThat(dto.getName()).isEqualTo("Отвертка");
        assertThat(dto.getDescription()).isEqualTo("Крестовая отвертка");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void validateRequestItemDto() {
        ItemDto dto = new ItemDto();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void serializeUpdateItemDto() throws Exception {
        UpdateItemDto dto = new UpdateItemDto();
        dto.setName("Дрель");
        dto.setDescription("Ударная дрель");
        dto.setAvailable(true);

        JsonContent<UpdateItemDto> result = updateJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ударная дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
    }

    @Test
    void deserializeUpdateItemDto() throws Exception {
        String content = "{\"name\":\"Отвертка\",\"description\":\"Крестовая отвертка\"}";

        UpdateItemDto dto = updateJson.parse(content).getObject();

        assertThat(dto.getName()).isEqualTo("Отвертка");
        assertThat(dto.getDescription()).isEqualTo("Крестовая отвертка");
        assertThat(dto.getAvailable()).isNull();
    }

    @Test
    void validateUpdateItemDto() {
        UpdateItemDto dto = new UpdateItemDto();

        Set<ConstraintViolation<UpdateItemDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void serializeRequestCommentDto() throws Exception {
        RequestCommentDto dto = new RequestCommentDto();
        dto.setText("Отличная вещь!");

        JsonContent<RequestCommentDto> result = commentJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличная вещь!");
    }

    @Test
    void deserializeRequestCommentDto() throws Exception {
        String content = "{\"text\":\"Хороший товар\"}";

        RequestCommentDto dto = commentJson.parse(content).getObject();

        assertThat(dto.getText()).isEqualTo("Хороший товар");
    }

    @Test
    void validateRequestCommentDto() {
        RequestCommentDto dto = new RequestCommentDto();

        Set<ConstraintViolation<RequestCommentDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}

