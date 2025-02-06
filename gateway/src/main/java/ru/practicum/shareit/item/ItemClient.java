package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(long userId, CreateItemRequest requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> updateItem(long itemId, long userId, UpdateItemRequest requestDto) {
        Map<String, Object> parameters = Map.of(
            "itemId", itemId
        );
        return patch("/{itemId}", userId, parameters, requestDto);
    }

    public ResponseEntity<Object> getById(long itemId) {
        Map<String, Object> parameters = Map.of(
            "itemId", itemId
        );
        return get("/{itemId}", null, parameters);
    }

    public ResponseEntity<Object> getByUserId(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> search(String text) {
        Map<String, Object> parameters = Map.of(
            "text", text
        );
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> createComment(long itemId, long userId, CreateCommentRequest requestDto) {
        Map<String, Object> parameters = Map.of(
            "itemId", itemId
        );
        return post("/{itemId}/comment", userId, parameters, requestDto);
    }
}
