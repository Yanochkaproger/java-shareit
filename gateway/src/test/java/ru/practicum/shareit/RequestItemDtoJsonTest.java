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
class RequestItemDtoJsonTest {

    @Autowired
    private JacksonTester<RequestItemDto> json;

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void serializeRequestItemDto() throws Exception {
        RequestItemDto dto = new RequestItemDto();
        dto.setDescription("Нужна дрель");

        JsonContent<RequestItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель");
    }

    @Test
    void deserializeRequestItemDto() throws Exception {
        String content = "{\"description\":\"Нужна отвертка\"}";

        RequestItemDto dto = json.parse(content).getObject();

        assertThat(dto.getDescription()).isEqualTo("Нужна отвертка");
    }

    @Test
    void validateBlankDescription() {
        RequestItemDto dto = new RequestItemDto();
        dto.setDescription("");

        Set<ConstraintViolation<RequestItemDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void validateNullDescription() {
        RequestItemDto dto = new RequestItemDto();

        Set<ConstraintViolation<RequestItemDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}

