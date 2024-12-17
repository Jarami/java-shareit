package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDto mapToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public static Item mapToItem(CreateItemRequest request, User owner) {
        return Item.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .build();
    }
}
