package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
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
    public ItemDto createItem(@RequestBody CreateItemRequest request, @RequestHeader("X-Sharer-User-Id") Long userId) {
        Item item = itemService.createItem(request, userId);
        return ItemMapper.mapToDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestBody UpdateItemRequest request,
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        Item item = itemService.updateItem(request, itemId, userId);
        return ItemMapper.mapToDto(item);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        Item item = itemService.getById(itemId);
        return ItemMapper.mapToDto(item);
    }

    @GetMapping
    public List<ItemDto> getByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getByUserId(userId).stream()
                .map(ItemMapper::mapToDto)
                .toList();
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam("text") String searchString) {
        return itemService.search(searchString).stream()
                .map(ItemMapper::mapToDto)
                .toList();
    }
}