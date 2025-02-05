package ru.practicum.shareit.itemRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper mapper;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createItemRequest(@Valid @RequestBody CreateItemRequestRequest request,
                                                            @RequestHeader("X-Sharer-User-Id") Long userId) {

        ItemRequest itemRequest = itemRequestService.createItemRequest(request, userId);
        return new ResponseEntity<>(mapper.toDto(itemRequest), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequest> requests = itemRequestService.getUserRequests(userId);
        return new ResponseEntity<>(mapper.toDto(requests), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequestsFromOthers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequest> requests = itemRequestService.getAllRequestsFromOthers(userId);
        return new ResponseEntity<>(mapper.toDto(requests), HttpStatus.OK);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequest(@PathVariable Long requestId) {
        ItemRequest itemRequest = itemRequestService.getRequest(requestId);
        return new ResponseEntity<>(mapper.toDto(itemRequest), HttpStatus.OK);
    }
}
