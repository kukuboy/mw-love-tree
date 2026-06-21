package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private Long id;
    private String url;
    private String caption;
    private Long uploaderId;
    private String uploaderNickname;
    private LocalDateTime createdAt;
}
