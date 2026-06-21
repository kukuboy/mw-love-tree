package com.lovetree.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lovetree.common.ApiResponse;
import com.lovetree.config.CurrentUser;
import com.lovetree.dto.PhotoResponse;
import com.lovetree.service.PhotoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public ApiResponse<IPage<PhotoResponse>> list(CurrentUser currentUser,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        IPage<PhotoResponse> response = photoService.list(
                currentUser.getCoupleId(), page, size);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<PhotoResponse> upload(CurrentUser currentUser,
                                              @RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) String caption) {
        PhotoResponse response = photoService.upload(
                currentUser.getCoupleId(), currentUser.getUserId(), file, caption);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(CurrentUser currentUser, @PathVariable Long id) {
        photoService.delete(id, currentUser.getCoupleId());
        return ApiResponse.success(null);
    }
}
