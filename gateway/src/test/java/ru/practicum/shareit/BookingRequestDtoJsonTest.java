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

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void serializeBookingRequestDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 2, 10, 0, 0);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(start);
        dto.setEnd(end);

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-06-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-06-02T10:00:00");
    }

    @Test
    void deserializeBookingRequestDto() throws Exception {
        String content = "{\"itemId\":2,\"start\":\"2026-07-01T12:00:00\",\"end\":\"2026-07-02T12:00:00\"}";

        BookingRequestDto dto = json.parse(content).getObject();

        assertThat(dto.getItemId()).isEqualTo(2L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 7, 1, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 7, 2, 12, 0, 0));
    }

    @Test
    void validateEndInPast() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().minusHours(1));

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void validateNullFields() {
        BookingRequestDto dto = new BookingRequestDto();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
    }
}
