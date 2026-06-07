package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemRequestServiceImplTest {

    private final ItemRequestService itemRequestService;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void testGetOtherUsersRequests() {
        var user1 = userService.createUser(makeUserDto("user1", "user1@mail.com"));
        var user2 = userService.createUser(makeUserDto("user2", "user2@mail.com"));

        itemRequestService.createRequest(makeRequestItem("123"), user1.getId());
        itemRequestService.createRequest(makeRequestItem("12345"), user1.getId());

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getOtherUsersRequests(user2.getId());

        assertThat(itemRequestDtos.size(), equalTo(2));
    }

    @Test
    void testAddItemToRequest() {
        var user = userService.createUser(makeUserDto("user", "user@mail.com"));

        ItemRequestDto request = itemRequestService.createRequest(makeRequestItem("Нужна дрель"), user.getId());

        ItemDto item = new ItemDto();
        item.setName("Дрель");
        item.setDescription("Ударная дрель");
        item.setAvailable(true);
        item.setRequestId(request.getId());

        itemService.createItem(item, user.getId());

        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), request.getId());

        assertThat(result.getItems(), notNullValue());
        assertThat(result.getItems().size(), equalTo(1));
        assertThat(result.getItems().getFirst().getName(), equalTo("Дрель"));
    }

    @Test
    void testGetUserRequests() {
        var user = userService.createUser(makeUserDto("user", "user_req@mail.com"));

        itemRequestService.createRequest(makeRequestItem("desc1"), user.getId());
        itemRequestService.createRequest(makeRequestItem("desc2"), user.getId());

        var result = itemRequestService.getUserRequests(user.getId());

        assertThat(result, hasSize(2));
    }

    private ItemRequestDto makeRequestItem(String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(description);
        return dto;
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }
}

