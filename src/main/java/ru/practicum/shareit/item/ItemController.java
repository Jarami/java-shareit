package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody CreateItemRequest request, @RequestHeader("X-Sharer-User-Id") Long userId) {
        Item item = itemService.createItem(request, userId);
        return new ResponseEntity<>(ItemMapper.mapToDto(item), HttpStatus.CREATED);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestBody UpdateItemRequest request,
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        Item item = itemService.updateItem(request, itemId, userId);
        return ResponseEntity.ok()
                .body(ItemMapper.mapToDto(item));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getById(@PathVariable Long itemId) {
        Item item = itemService.getById(itemId);
        return ResponseEntity.ok()
                .body(ItemMapper.mapToDto(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok()
                .body(
                        itemService.getByUserId(userId).stream()
                            .map(ItemMapper::mapToDto)
                            .toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam("text") String searchString) {
        return ResponseEntity.ok()
                .body(
                        itemService.search(searchString).stream()
                            .map(ItemMapper::mapToDto)
                            .toList());
    }
}