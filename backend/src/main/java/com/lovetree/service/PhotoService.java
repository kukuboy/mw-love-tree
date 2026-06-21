package com.lovetree.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lovetree.dto.PhotoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

    PhotoResponse upload(Long coupleId, Long uploaderId, MultipartFile file, String caption);

    IPage<PhotoResponse> list(Long coupleId, int page, int size);

    void delete(Long photoId, Long coupleId);
}
