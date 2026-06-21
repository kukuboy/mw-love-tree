package com.lovetree.service;

import com.lovetree.dto.MessageResponse;
import com.lovetree.dto.SendMessageRequest;

import java.util.List;

public interface MessageService {

    List<MessageResponse> getMessages(Long coupleId, Long userId);

    MessageResponse send(Long coupleId, Long fromId, SendMessageRequest request);

    Integer getUnreadCount(Long coupleId, Long userId);

    void markRead(Long messageId, Long coupleId);
}
