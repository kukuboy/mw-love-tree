package com.lovetree.controller;

import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.dto.MessageResponse;
import com.lovetree.dto.SendMessageRequest;
import com.lovetree.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<MessageResponse>> list(CurrentUser currentUser) {
        List<MessageResponse> messages = messageService.getMessages(
                currentUser.getCoupleId(), currentUser.getUserId());
        return ApiResponse.success(messages);
    }

    @PostMapping
    public ApiResponse<MessageResponse> send(CurrentUser currentUser,
                                              @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.send(
                currentUser.getCoupleId(), currentUser.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(CurrentUser currentUser) {
        Integer count = messageService.getUnreadCount(
                currentUser.getCoupleId(), currentUser.getUserId());
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(CurrentUser currentUser, @PathVariable Long id) {
        messageService.markRead(id, currentUser.getCoupleId());
        return ApiResponse.success(null);
    }
}
