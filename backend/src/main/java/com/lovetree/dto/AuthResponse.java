package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String nickname;
    private Long coupleId;
    private String avatar;
}
