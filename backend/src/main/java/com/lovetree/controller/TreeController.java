package com.lovetree.controller;

import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.dto.TreeResponse;
import com.lovetree.dto.TreeStatsResponse;
import com.lovetree.service.TreeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tree")
public class TreeController {

    private final TreeService treeService;

    public TreeController(TreeService treeService) {
        this.treeService = treeService;
    }

    @GetMapping
    public ApiResponse<TreeResponse> getTree(CurrentUser currentUser) {
        TreeResponse response = treeService.getTree(currentUser.getCoupleId());
        return ApiResponse.success(response);
    }

    @GetMapping("/stats")
    public ApiResponse<TreeStatsResponse> getStats(CurrentUser currentUser) {
        TreeStatsResponse response = treeService.getStats(currentUser.getCoupleId());
        return ApiResponse.success(response);
    }
}
