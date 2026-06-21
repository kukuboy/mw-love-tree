package com.lovetree.service.impl;

import com.lovetree.common.BusinessException;
import com.lovetree.dto.MessageResponse;
import com.lovetree.dto.SendMessageRequest;
import com.lovetree.entity.Couple;
import com.lovetree.entity.Message;
import com.lovetree.entity.User;
import com.lovetree.mapper.CoupleMapper;
import com.lovetree.mapper.MessageMapper;
import com.lovetree.mapper.UserMapper;
import com.lovetree.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final CoupleMapper coupleMapper;
    private final UserMapper userMapper;

    public MessageServiceImpl(MessageMapper messageMapper,
                              CoupleMapper coupleMapper,
                              UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.coupleMapper = coupleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<MessageResponse> getMessages(Long coupleId, Long userId) {
        return messageMapper.selectMessages(coupleId, userId);
    }

    @Override
    @Transactional
    public MessageResponse send(Long coupleId, Long fromId, SendMessageRequest request) {
        Couple couple = coupleMapper.selectById(coupleId);
        if (couple == null) {
            throw new BusinessException(404, "Couple not found");
        }

        Long partnerId;
        if (couple.getUser1Id().equals(fromId)) {
            partnerId = couple.getUser2Id();
        } else if (couple.getUser2Id() != null && couple.getUser2Id().equals(fromId)) {
            partnerId = couple.getUser1Id();
        } else {
            throw new BusinessException(403, "User is not a member of this couple");
        }

        if (partnerId == null) {
            throw new BusinessException(400, "Partner has not joined yet");
        }

        Message message = new Message();
        message.setCoupleId(coupleId);
        message.setFromId(fromId);
        message.setToId(partnerId);
        message.setContent(request.getContent());
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        messageMapper.insert(message);

        User fromUser = userMapper.selectById(fromId);
        String fromNickname = fromUser != null ? fromUser.getNickname() : null;

        return new MessageResponse(
                message.getId(),
                message.getFromId(),
                message.getToId(),
                message.getContent(),
                message.getIsRead(),
                message.getCreatedAt(),
                fromNickname
        );
    }

    @Override
    public Integer getUnreadCount(Long coupleId, Long userId) {
        Integer count = messageMapper.countUnread(coupleId, userId);
        return count != null ? count : 0;
    }

    @Override
    public void markRead(Long messageId, Long coupleId) {
        int affected = messageMapper.markRead(messageId, coupleId);
        if (affected == 0) {
            throw new BusinessException(404, "Message not found or access denied");
        }
    }
}
