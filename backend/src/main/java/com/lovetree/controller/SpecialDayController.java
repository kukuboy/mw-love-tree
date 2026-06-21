package com.lovetree.controller;

import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.dto.SpecialDayResponse;
import com.lovetree.service.SpecialDayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/special-days")
public class SpecialDayController {

    private final SpecialDayService specialDayService;

    public SpecialDayController(SpecialDayService specialDayService) {
        this.specialDayService = specialDayService;
    }

    @GetMapping
    public ApiResponse<List<SpecialDayResponse>> getSpecialDays(CurrentUser currentUser) {
        List<SpecialDayResponse> response = specialDayService.getSpecialDays(currentUser.getCoupleId());
        return ApiResponse.success(response);
    }
}
