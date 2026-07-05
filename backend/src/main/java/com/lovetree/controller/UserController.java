package com.lovetree.controller;

import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(CurrentUser currentUser,
                                                          @RequestParam("file") MultipartFile file) {
        String avatarUrl = authService.uploadAvatar(currentUser.getUserId(), file);
        return ApiResponse.success(Map.of("avatar", avatarUrl));
    }
}
