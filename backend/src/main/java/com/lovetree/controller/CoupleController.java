package com.lovetree.controller;

import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.dto.CoupleInfoResponse;
import com.lovetree.dto.InviteResponse;
import com.lovetree.dto.JoinRequest;
import com.lovetree.service.CoupleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/couple")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    @PostMapping("/invite")
    public ApiResponse<InviteResponse> invite(CurrentUser currentUser) {
        String inviteCode = coupleService.generateInvite(currentUser.getUserId());
        return ApiResponse.success(new InviteResponse(inviteCode));
    }

    @PostMapping("/join")
    public ApiResponse<Map<String, String>> join(CurrentUser currentUser, @Valid @RequestBody JoinRequest request) {
        String newToken = coupleService.join(currentUser.getUserId(), request.getInviteCode());
        return ApiResponse.success(Map.of("token", newToken));
    }

    @GetMapping("/info")
    public ApiResponse<CoupleInfoResponse> info(CurrentUser currentUser) {
        CoupleInfoResponse response = coupleService.getInfo(
                currentUser.getCoupleId(), currentUser.getUserId());
        return ApiResponse.success(response);
    }
}
