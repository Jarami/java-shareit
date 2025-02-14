package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

@Slf4j
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody CreateItemRequest request,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Create item {}, userId={}", request, userId);
        return itemClient.createItem(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @Valid @RequestBody UpdateItemRequest request,
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Update item {}, userId={}", request, userId);
        return itemClient.updateItem(itemId, userId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId) {
        log.info("Get item {}", itemId);
        return itemClient.getById(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get items, userId={}", userId);
        return itemClient.getByUserId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam("text") String searchString) {
        log.info("Search items, text={}", searchString);
        return itemClient.search(searchString);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@Valid @RequestBody CreateCommentRequest request,
                                                @PathVariable Long itemId,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Create comment {}, itemId={}, userId={}", request, itemId, userId);
        return itemClient.createComment(itemId, userId, request);
    }
}
