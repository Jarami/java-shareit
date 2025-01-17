package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepo {

    Item save(Item item);

    Optional<Item> getById(Long id);

    List<Item> getByUserId(long userId);

    List<Item> search(String substring);

    void deleteById(Long itemId);
}
