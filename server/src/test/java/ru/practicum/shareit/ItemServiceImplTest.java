package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.RequestBookingDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ResponseItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemServiceImplTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void testGetOwnerItems() {
        var user1 = userService.createUser(makeUserDto("Ivan", "ivan@gmail.com"));
        var user2 = userService.createUser(makeUserDto("Dmitry", "dmitry@gmail.com"));

        var item1 = itemService.createItem(makeItemDto("Item1", "1", true), user1.getId());
        var item2 = itemService.createItem(makeItemDto("Item2", "12", true), user1.getId());
        var item3 = itemService.createItem(makeItemDto("Item3", "123", true), user1.getId());

        var booking1 = bookingService.createBooking(
                makeBookingDto(item1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
                user2.getId()
        );
        bookingService.resolveBooking(user1.getId(), booking1.getId(), true);

        List<ResponseItemDto> userItems = itemService.getOwnerItems(user1.getId());

        assertThat(userItems.size(), equalTo(3));
        assertThat(userItems, containsInAnyOrder(
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("name", equalTo(item2.getName())),
                hasProperty("name", equalTo(item3.getName()))
        ));

        var itemWithBooking = userItems.stream()
                .filter(i -> i.getId().equals(item1.getId()))
                .findFirst().orElseThrow();

        assertThat(itemWithBooking.getLastBooking(), nullValue());
        assertThat(itemWithBooking.getNextBooking(), notNullValue());
    }

    @Test
    void testUpdateItem() {
        var owner = userService.createUser(makeUserDto("Owner", "owner@mail.com"));
        var item = itemService.createItem(makeItemDto("Original", "original desc", true), owner.getId());

        var updated = new ItemDto();
        updated.setName("Updated");
        updated.setDescription("updated desc");
        var result = itemService.updateItem(updated, item.getId(), owner.getId());

        assertThat(result.getName(), equalTo("Updated"));
        assertThat(result.getDescription(), equalTo("updated desc"));
    }

    @Test
    void testGetItemById() {
        var owner = userService.createUser(makeUserDto("Owner", "owner2@mail.com"));
        var item = itemService.createItem(makeItemDto("Item", "desc", true), owner.getId());

        var result = itemService.getItemById(item.getId());

        assertThat(result.getName(), equalTo("Item"));
    }

    @Test
    void testSearchAvailableItems() {
        var owner = userService.createUser(makeUserDto("Owner", "owner3@mail.com"));
        itemService.createItem(makeItemDto("Screwdriver", "A handy tool", true), owner.getId());
        itemService.createItem(makeItemDto("Hammer", "A heavy tool", false), owner.getId());

        var results = itemService.searchAvailableItems("tool");

        assertThat(results.size(), equalTo(1));
        assertThat(results.getFirst().getName(), equalTo("Screwdriver"));
    }

    @Test
    void testCreateComment() {
        var owner = userService.createUser(makeUserDto("Owner", "owner4@mail.com"));
        var booker = userService.createUser(makeUserDto("Booker", "booker4@mail.com"));
        var item = itemService.createItem(makeItemDto("Item", "desc", true), owner.getId());

        var booking = bookingService.createBooking(
                makeBookingDto(item.getId(), LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3)),
                booker.getId()
        );
        bookingService.resolveBooking(owner.getId(), booking.getId(), true);

        var commentDto = new CommentDto();
        commentDto.setText("Great item!");
        var result = itemService.createComment(booker.getId(), item.getId(), commentDto);

        assertThat(result.getText(), equalTo("Great item!"));
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

