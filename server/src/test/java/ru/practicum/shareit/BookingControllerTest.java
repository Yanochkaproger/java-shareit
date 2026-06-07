package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.RequestBookingDto;
import ru.practicum.shareit.booking.ResponseBookingDto;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ShortItemDto;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService bookingService;

    private ResponseBookingDto responseBookingDto;
    private RequestBookingDto requestBookingDto;

    @BeforeEach
    void beforeEach() {
        UserDto booker = new UserDto();
        booker.setId(2L);
        booker.setName("Петя");
        booker.setEmail("petya@mail.com");

        ShortItemDto item = new ShortItemDto();
        item.setId(1L);
        item.setName("Вещь");
        item.setOwnerId(1L);

        ResponseBookingDto responseDto = new ResponseBookingDto();
        responseDto.setId(1L);
        responseDto.setStart(LocalDateTime.now().plusHours(1));
        responseDto.setEnd(LocalDateTime.now().plusDays(1));
        responseDto.setStatus(BookingStatus.WAITING);
        responseDto.setBooker(booker);
        responseDto.setItem(item);
        responseBookingDto = responseDto;

        RequestBookingDto requestDto = new RequestBookingDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));
        requestBookingDto = requestDto;
    }

    private List<ResponseBookingDto> makeBookingDtoList() {
        UserDto booker = new UserDto();
        booker.setId(2L);
        booker.setName("Петя");
        booker.setEmail("petya@mail.com");

        ShortItemDto item1 = new ShortItemDto();
        item1.setId(1L);
        item1.setName("Вещь 1");
        item1.setOwnerId(1L);

        ShortItemDto item2 = new ShortItemDto();
        item2.setId(2L);
        item2.setName("Вещь 2");
        item2.setOwnerId(1L);

        ResponseBookingDto dto1 = new ResponseBookingDto();
        dto1.setId(1L);
        dto1.setStart(LocalDateTime.now().plusHours(1));
        dto1.setEnd(LocalDateTime.now().plusDays(1));
        dto1.setStatus(BookingStatus.APPROVED);
        dto1.setBooker(booker);
        dto1.setItem(item1);

        ResponseBookingDto dto2 = new ResponseBookingDto();
        dto2.setId(2L);
        dto2.setStart(LocalDateTime.now().plusDays(2));
        dto2.setEnd(LocalDateTime.now().plusDays(3));
        dto2.setStatus(BookingStatus.WAITING);
        dto2.setBooker(booker);
        dto2.setItem(item2);

        return List.of(dto1, dto2);
    }

    @Test
    void createBooking() throws Exception {
        when(bookingService.createBooking(any(), anyLong()))
                .thenReturn(responseBookingDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.name())));
    }

    @Test
    void resolveBooking() throws Exception {
        responseBookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.resolveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseBookingDto);

        mvc.perform(patch("/bookings/" + responseBookingDto.getId())
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.name())));
    }

    @Test
    void getBooking() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenReturn(responseBookingDto);

        mvc.perform(get("/bookings/" + responseBookingDto.getId())
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.name())));
    }

    @Test
    void getUserBookings() throws Exception {
        List<ResponseBookingDto> dtoList = makeBookingDtoList();

        when(bookingService.getUserBookings(anyLong(), any()))
                .thenReturn(dtoList);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(
                        BookingStatus.APPROVED.name(), BookingStatus.WAITING.name())));
    }

    @Test
    void getOwnerBookings() throws Exception {
        List<ResponseBookingDto> dtoList = makeBookingDtoList();

        when(bookingService.getOwnerBookings(anyLong(), any()))
                .thenReturn(dtoList);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(
                        BookingStatus.APPROVED.name(), BookingStatus.WAITING.name())));
    }
}

