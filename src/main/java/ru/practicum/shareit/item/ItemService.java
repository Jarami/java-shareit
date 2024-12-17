package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepo;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class ItemService {

    private final UserService userService;
    private final ItemRepo repo;

    public Item createItem(@Valid CreateItemRequest request, long userId) {

        User owner = userService.getById(userId);
        Item item = ItemMapper.mapToItem(request, owner);
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
        return repo.getById(itemId)
                .orElseThrow(() -> new NotFoundException("не найдена вещь с id = %s", itemId));
    }

    public List<Item> getByUserId(long userId) {
        return repo.getByUserId(userId);
    }

    public List<Item> search(String searchString) {
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