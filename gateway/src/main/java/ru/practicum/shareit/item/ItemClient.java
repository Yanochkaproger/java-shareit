package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.RequestCommentDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.UpdateItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(ItemDto dto, Long userId) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> updateItem(UpdateItemDto dto, Long userId, Long itemId) {
        return patch("/" + itemId, userId, dto);
    }

    public ResponseEntity<Object> getItem(Long itemId) {
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getItems(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        Map<String, Object> params = Map.of("text", text);

        return get("/search", null, params);
    }

    public ResponseEntity<Object> createComment(RequestCommentDto commentDto, Long userId, Long itemId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}

