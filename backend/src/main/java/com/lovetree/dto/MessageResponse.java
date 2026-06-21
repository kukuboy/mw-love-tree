package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long fromId;
    private Long toId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String fromNickname;
}
