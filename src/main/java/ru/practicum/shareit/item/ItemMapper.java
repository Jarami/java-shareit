package ru.practicum.shareit.item;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentRequest;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ItemMapper {

    @Mapping(target = "lastBooking", source = "lastBooking", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "nextBooking", source = "nextBooking", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    ItemDto toDto(Item item);

    List<ItemDto> toDto(List<Item> item);

    Item toItem(CreateItemRequest request);

    Comment toComment(CreateCommentRequest request);

    @Mapping(target = "authorName", source = "comment.author.name")
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDto(List<Comment> comments);
}
