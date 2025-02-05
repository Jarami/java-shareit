package ru.practicum.shareit.itemRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.itemRequest.dto.CreateItemRequestRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final UserService userService;
    private final ItemRequestMapper mapper;
    private final ItemRequestRepository repo;

    public ItemRequest createItemRequest(CreateItemRequestRequest request, Long userId) {
        log.info("creating request {} by {}", request, userId);

        User user = userService.getById(userId);

        ItemRequest itemRequest = mapper.toItemRequest(request, user);
        return repo.save(itemRequest);
    }

    public List<ItemRequest> getUserRequests(Long userId) {
        log.info("getting user {} requests", userId);
        User user = userService.getById(userId);
        return repo.findAllByRequesterWithItems(user);
    }

    public List<ItemRequest> getAllRequestsFromOthers(Long userId) {
        log.info("getting requests from others (user id is {})", userId);
        User user = userService.getById(userId);
        return repo.findAllRequesterNotOrderByCreatedDesc(user);
    }

    public ItemRequest getRequest(Long requestId) {
        log.info("getting request {}", requestId);
        return repo.findByIdWithItems(requestId)
                .orElseThrow(() -> new NotFoundException("не найден запрос с id = %s", requestId));
    }
}
