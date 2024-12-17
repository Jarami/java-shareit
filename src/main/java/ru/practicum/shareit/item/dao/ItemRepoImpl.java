package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemRepoImpl implements ItemRepo {

    private final Map<Long, Item> items = new HashMap<>();
    private long counter = 0;

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            long id = generateId();
            item.setId(id);
            items.put(id, item);
        } else {
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Optional<Item> getById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> getByUserId(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> search(String substring) {
        return items.values().stream()
                .filter(item -> {
                    if (substring.trim().isEmpty()) {
                        return false;
                    }
                    String lowerSubstring = substring.toLowerCase();
                    return item.getName().toLowerCase().contains(lowerSubstring) ||
                            item.getDescription().toLowerCase().contains(lowerSubstring);
                })
                .toList();
    }

    @Override
    public void deleteById(Long itemId) {
        items.remove(itemId);
    }

    private long generateId() {
        return counter++;
    }
}
