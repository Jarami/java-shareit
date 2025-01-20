package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ItemService {

    private final UserService userService;
    private final ItemRepository repo;
    private final ItemMapper mapper;
    private final CommentRepository commentRepository;

    public Item createItem(@Valid CreateItemRequest request, long userId) {

        User owner = userService.getById(userId);
        Item item = mapper.toItem(request);
        item.setOwner(owner);
        return repo.save(item);
    }

    public Item updateItem(@Valid UpdateItemRequest request, long itemId, long userId) {

        Item item = getById(itemId);
        User owner = userService.getById(userId);

        if (!item.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("пользователю %s запрещено изменять вещь %s", userId, itemId);
        }

        String newName = request.getName();
        String newDescription = request.getDescription();
        Boolean newAvailable = request.getAvailable();

        if (newName != null) {
            item.setName(newName);
        }

        if (newDescription != null) {
            item.setDescription(newDescription);
        }

        if (newAvailable != null) {
            item.setAvailable(newAvailable);
        }

        return repo.save(item);
    }

    public Item getById(long itemId) {
        return repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("не найдена вещь с id = %s", itemId));
    }

    public List<Item> getByUserId(long userId) {
        User owner = userService.getById(userId);
        return repo.findAllByOwnerWithPastNextBooking(owner, LocalDateTime.now());
    }

    public List<Item> search(String searchString) {
        if (searchString == null || searchString.isBlank()) {
            return List.of();
        }
        return repo.search(searchString);
    }

    public void deleteById(long itemId, long userId) {

        Item item = getById(itemId);
        User owner = userService.getById(userId);

        if (!item.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("пользователю %s запрещено удалять вещь %s", userId, itemId);
        }

        repo.deleteById(itemId);
    }
}
