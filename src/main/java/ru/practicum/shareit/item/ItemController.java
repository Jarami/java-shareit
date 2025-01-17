package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemMapper mapper;
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody CreateItemRequest request,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {

        Item item = itemService.createItem(request, userId);
        return new ResponseEntity<>(mapper.toDto(item), HttpStatus.CREATED);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestBody UpdateItemRequest request,
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
    public ResponseEntity<List<ItemDto>> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<Item> items = itemService.getByUserId(userId);
        return ResponseEntity.ok().body(mapper.toDto(items));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam("text") String searchString) {
        List<Item> items = itemService.search(searchString);
        return ResponseEntity.ok().body(mapper.toDto(items));
    }
}