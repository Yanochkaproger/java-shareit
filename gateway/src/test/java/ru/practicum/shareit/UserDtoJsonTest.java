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
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<RequestUserDto> requestJson;

    @Autowired
    private JacksonTester<UpdateUserDto> updateJson;

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void serializeRequestUserDto() throws Exception {
        RequestUserDto dto = new RequestUserDto();
        dto.setName("Петя");
        dto.setEmail("petya@mail.com");

        JsonContent<RequestUserDto> result = requestJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Петя");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("petya@mail.com");
    }

    @Test
    void deserializeRequestUserDto() throws Exception {
        String content = "{\"name\":\"Вася\",\"email\":\"vasya@mail.com\"}";

        RequestUserDto dto = requestJson.parse(content).getObject();

        assertThat(dto.getName()).isEqualTo("Вася");
        assertThat(dto.getEmail()).isEqualTo("vasya@mail.com");
    }

    @Test
    void validateRequestUserDto() {
        RequestUserDto dto = new RequestUserDto();

        Set<ConstraintViolation<RequestUserDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void validateRequestUserDtoBadEmail() {
        RequestUserDto dto = new RequestUserDto();
        dto.setName("Петя");
        dto.setEmail("not-an-email");

        Set<ConstraintViolation<RequestUserDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void serializeUpdateUserDto() throws Exception {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("Петя");
        dto.setEmail("petya@mail.com");

        JsonContent<UpdateUserDto> result = updateJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Петя");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("petya@mail.com");
    }

    @Test
    void deserializeUpdateUserDto() throws Exception {
        String content = "{\"name\":\"Вася\"}";

        UpdateUserDto dto = updateJson.parse(content).getObject();

        assertThat(dto.getName()).isEqualTo("Вася");
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void validateUpdateUserDtoBadEmail() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("Петя");
        dto.setEmail("bad-email");

        Set<ConstraintViolation<UpdateUserDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}
