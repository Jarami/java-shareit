package ru.practicum.shareit.itemRequest;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ItemRequestMapper {

    @Mapping(target = "created", source = "created", dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ")
    ItemRequestDto toDto(ItemRequest request);

    List<ItemRequestDto> toDto(List<ItemRequest> requests);

    @Mapping(target = "requester", source = "user")
    ItemRequest toItemRequest(CreateItemRequestRequest request, User user);

    @Mapping(target = "ownerId", source = "owner.id")
    ItemRequestDto.ItemDto toItemDto(Item item);
}
