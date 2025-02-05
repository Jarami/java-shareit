package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemMapper mapper;
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody CreateItemRequest request,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {

        Item item = itemService.createItem(request, userId);
        return new ResponseEntity<>(mapper.toDto(item), HttpStatus.CREATED);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @Valid @RequestBody UpdateItemRequest request,
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        Item item = itemService.updateItem(request, itemId, userId);
        return ResponseEntity.ok().body(mapper.toDto(item));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getById(@PathVariable Long itemId) {
        Item item = itemService.getById(itemId);
        return ResponseEntity.ok().body(mapper.toDto(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now) {
        List<Item> items = itemService.getByUserId(userId, now);
        return ResponseEntity.ok().body(mapper.toDto(items));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam("text") String searchString) {
        List<Item> items = itemService.search(searchString);
        return ResponseEntity.ok().body(mapper.toDto(items));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CreateCommentRequest request,
                                                    @PathVariable Long itemId,
                                                    @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("creating comment on item {} by {} at {}", itemId, userId, now);
        Comment comment = commentService.createComment(request, itemId, userId, now);
        return new ResponseEntity<>(mapper.toCommentDto(comment), HttpStatus.CREATED);
    }
}