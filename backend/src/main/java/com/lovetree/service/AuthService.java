package com.lovetree.service;

import com.lovetree.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(String email, String password, String nickname);
    AuthResponse login(String email, String password);
}
