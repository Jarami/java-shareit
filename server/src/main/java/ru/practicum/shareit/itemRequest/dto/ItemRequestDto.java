package ru.practicum.shareit.itemRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private long id;
    private String description;
    private List<ItemDto> items = new ArrayList<>();
    private String created;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private Long id;
        private String name;
        private Long ownerId;
    }
}
