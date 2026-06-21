package com.lovetree.controller;

import com.lovetree.common.ApiResponse;
import com.lovetree.dto.AuthResponse;
import com.lovetree.dto.LoginRequest;
import com.lovetree.dto.RegisterRequest;
import com.lovetree.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(
                request.getEmail(),
                request.getPassword()
        );
        return ApiResponse.success(response);
    }
}
