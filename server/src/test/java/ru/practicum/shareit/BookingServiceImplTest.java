package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.RequestBookingDto;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class BookingServiceImplTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void testGetUserBookings() {
        var owner = userService.createUser(makeUserDto("Owner", "owner@mail.com"));
        var booker = userService.createUser(makeUserDto("Booker", "booker@mail.com"));
        var item = itemService.createItem(makeItemDto("Item", "desc", true), owner.getId());

        var pastBooking = bookingService.createBooking(
                makeBookingDto(item.getId(), LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3)),
                booker.getId()
        );
        bookingService.resolveBooking(owner.getId(), pastBooking.getId(), true);

        bookingService.createBooking(
                makeBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
                booker.getId()
        );

        var rejectedBooking = bookingService.createBooking(
                makeBookingDto(item.getId(), LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4)),
                booker.getId()
        );
        bookingService.resolveBooking(owner.getId(), rejectedBooking.getId(), false);

        var currentBooking = bookingService.createBooking(
                makeBookingDto(item.getId(), LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(2)),
                booker.getId()
        );
        bookingService.resolveBooking(owner.getId(), currentBooking.getId(), true);

        var asBooker = bookingService.getUserBookings(booker.getId(), BookingState.ALL);
        assertThat(asBooker.size(), equalTo(4));
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.FUTURE).size(), equalTo(2));
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.PAST).size(), equalTo(1));
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.WAITING).size(), equalTo(1));
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.REJECTED).size(), equalTo(1));

        var asOwner = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);
        assertThat(asOwner.size(), equalTo(4));
        assertThat(bookingService.getOwnerBookings(owner.getId(), BookingState.WAITING).size(), equalTo(1));
        assertThat(bookingService.getOwnerBookings(owner.getId(), BookingState.CURRENT).size(), equalTo(1));
    }

    @Test
    void testGetBooking() {
        var owner = userService.createUser(makeUserDto("Owner", "own@mail.com"));
        var booker = userService.createUser(makeUserDto("Booker", "book@mail.com"));
        var item = itemService.createItem(makeItemDto("Item", "desc", true), owner.getId());

        var booking = bookingService.createBooking(
                makeBookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
                booker.getId()
        );

        var asOwner = bookingService.getBooking(owner.getId(), booking.getId());
        assertThat(asOwner.getId(), equalTo(booking.getId()));

        var asBooker = bookingService.getBooking(booker.getId(), booking.getId());
        assertThat(asBooker.getId(), equalTo(booking.getId()));

        var stranger = userService.createUser(makeUserDto("Stranger", "str@mail.com"));
        assertThrows(Exception.class, () ->
                bookingService.getBooking(stranger.getId(), booking.getId())
        );
    }

    private ItemDto makeItemDto(String name, String description, boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }

    private RequestBookingDto makeBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        RequestBookingDto bookingDto = new RequestBookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }
}

