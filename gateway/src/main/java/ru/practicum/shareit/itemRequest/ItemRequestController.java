package ru.practicum.shareit.itemRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody CreateItemRequestRequest request,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Create item request {}, userId={}", request, userId);
        return itemRequestClient.createItemRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get user requests, userId={}", userId);
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestsFromOthers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get other user requests, userId={}", userId);
        return itemRequestClient.getAllRequestsFromOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@PathVariable Long requestId) {
        log.info("Get request {}", requestId);
        return itemRequestClient.getRequest(requestId);
    }
}
