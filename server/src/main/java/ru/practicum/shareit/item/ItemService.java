package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemLastNextBookDate;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.itemRequest.ItemRequest;
import ru.practicum.shareit.itemRequest.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final UserService userService;
    private final ItemRepository repo;
    private final ItemMapper mapper;
    private final CommentRepository commentRepository;
    private final ItemRequestService itemRequestService;

    @Transactional
    public Item createItem(CreateItemRequest request, long userId) {

        User owner = userService.getById(userId);
        Item item = mapper.toItem(request);
        item.setOwner(owner);

        if (request.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestService.getById(request.getRequestId());
            item.setItemRequest(itemRequest);
        }

        return repo.save(item);
    }

    @Transactional
    public Item updateItem(UpdateItemRequest request, long itemId, long userId) {

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

    public List<Item> getByUserId(long userId, LocalDateTime now) {

        User owner = userService.getById(userId);

        Map<Long, ItemLastNextBookDate> itemById = groupById(repo.getLastAndNextBookingDate(owner, now));

        List<Item> items = repo.findAllByOwnerWithComments(owner);
        items.forEach(item -> {
            if (itemById.get(item.getId()) != null) {
                ItemLastNextBookDate date = itemById.get(item.getId());
                item.setLastBooking(date.getLastBooking());
                item.setNextBooking(date.getNextBooking());
            }
        });

        return items;
    }

    public List<Item> search(String searchString) {
        if (searchString == null || searchString.isBlank()) {
            return List.of();
        }
        return repo.search(searchString);
    }

    @Transactional
    public void deleteById(long itemId, long userId) {

        Item item = getById(itemId);
        User owner = userService.getById(userId);

        if (!item.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("пользователю %s запрещено удалять вещь %s", userId, itemId);
        }

        repo.deleteById(itemId);
    }

    private Map<Long, ItemLastNextBookDate> groupById(List<ItemLastNextBookDate> items) {
        Map<Long, ItemLastNextBookDate> itemById = new HashMap<>();
        items.forEach(item -> itemById.put(item.getId(), item));
        return itemById;
    }
}


