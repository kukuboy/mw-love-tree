package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private Long coupleId;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String content;
    private String eventType;
    private LocalDate eventDate;
    private List<String> images;
    private String mood;
    private LocalDateTime createdAt;
}
