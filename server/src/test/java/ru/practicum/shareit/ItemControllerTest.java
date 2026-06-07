package ru.practicum.shareit;

import ru.practicum.shareit.item.ItemController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ResponseItemDto;
import ru.practicum.shareit.item.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ResponseItemDto responseItemDto;

    @BeforeEach
    void beforeEach() {
        ItemDto newItem = new ItemDto();
        newItem.setName("Вещь");
        newItem.setDescription("123");
        newItem.setAvailable(false);
        itemDto = newItem;

        ResponseItemDto newResponse = new ResponseItemDto();
        newResponse.setId(1L);
        newResponse.setName("Вещь");
        newResponse.setDescription("123");
        newResponse.setAvailable(false);
        responseItemDto = newResponse;
    }

    private List<ResponseItemDto> makeItemDtoList() {
        ResponseItemDto responseItemDto1 = new ResponseItemDto();
        responseItemDto1.setId(1L);
        responseItemDto1.setName("Вещь 1");
        responseItemDto1.setDescription("Описание 1");
        responseItemDto1.setAvailable(false);

        ResponseItemDto responseItemDto2 = new ResponseItemDto();
        responseItemDto2.setId(2L);
        responseItemDto2.setName("Вещь 2");
        responseItemDto2.setDescription("Описание 2");
        responseItemDto2.setAvailable(true);

        return List.of(responseItemDto1, responseItemDto2);
    }

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(any(), anyLong()))
                .thenReturn(responseItemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseItemDto.getName())))
                .andExpect(jsonPath("$.description", is(responseItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(responseItemDto.getAvailable())));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(any(), anyLong(), anyLong()))
                .thenReturn(responseItemDto);

        mvc.perform(patch("/items/" + responseItemDto.getId())
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseItemDto.getName())))
                .andExpect(jsonPath("$.description", is(responseItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(responseItemDto.getAvailable())));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItemById(anyLong()))
                .thenReturn(responseItemDto);

        mvc.perform(get("/items/" + responseItemDto.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseItemDto.getName())))
                .andExpect(jsonPath("$.description", is(responseItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(responseItemDto.getAvailable())));
    }

    @Test
    void getOwnerItems() throws Exception {
        List<ResponseItemDto> responseItemDtoList = makeItemDtoList();

        when(itemService.getOwnerItems(anyLong()))
                .thenReturn(responseItemDtoList);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Вещь 1", "Вещь 2")));
    }

    @Test
    void searchItems() throws Exception {
        List<ResponseItemDto> responseItemDtoList = makeItemDtoList();

        when(itemService.searchAvailableItems(anyString()))
                .thenReturn(responseItemDtoList);

        mvc.perform(get("/items/search")
                        .param("text", "anytext")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Вещь 1", "Вещь 2")));
    }

    @Test
    void createComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("some comment");
        commentDto.setAuthorName("unknown");

        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }
}
