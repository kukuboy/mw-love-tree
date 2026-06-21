package com.lovetree.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.dto.CreateEventRequest;
import com.lovetree.dto.EventResponse;
import com.lovetree.service.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ApiResponse<IPage<EventResponse>> list(CurrentUser currentUser,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String eventType) {
        IPage<EventResponse> response = eventService.list(
                currentUser.getCoupleId(), page, size, eventType);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<EventResponse> create(CurrentUser currentUser,
                                             @Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.create(
                currentUser.getCoupleId(), currentUser.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getById(CurrentUser currentUser, @PathVariable Long id) {
        EventResponse response = eventService.getById(id, currentUser.getCoupleId());
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<EventResponse> update(CurrentUser currentUser,
                                             @PathVariable Long id,
                                             @Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.update(
                id, currentUser.getCoupleId(), currentUser.getUserId(), request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(CurrentUser currentUser, @PathVariable Long id) {
        eventService.delete(id, currentUser.getCoupleId());
        return ApiResponse.success(null);
    }
}
