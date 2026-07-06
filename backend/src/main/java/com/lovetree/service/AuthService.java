package com.lovetree.service;

import com.lovetree.dto.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AuthResponse register(String email, String password, String nickname);
    AuthResponse login(String email, String password);
    String uploadAvatar(Long userId, MultipartFile file);
}
