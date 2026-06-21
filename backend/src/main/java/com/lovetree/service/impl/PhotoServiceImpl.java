package com.lovetree.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lovetree.common.BusinessException;
import com.lovetree.dto.PhotoResponse;
import com.lovetree.entity.Photo;
import com.lovetree.mapper.PhotoMapper;
import com.lovetree.service.PhotoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png", "gif", "webp");

    @Value("${lovetree.upload.dir}")
    private String uploadDir;

    private final PhotoMapper photoMapper;

    public PhotoServiceImpl(PhotoMapper photoMapper) {
        this.photoMapper = photoMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @Override
    @Transactional
    public PhotoResponse upload(Long coupleId, Long uploaderId, MultipartFile file, String caption) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(400, "File name is required");
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new BusinessException(400, "File must have an extension");
        }

        String extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "Invalid file extension. Allowed: jpg, png, gif, webp");
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path targetPath = Paths.get(uploadDir, filename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "Failed to save file");
        }

        Photo photo = new Photo();
        photo.setCoupleId(coupleId);
        photo.setUploaderId(uploaderId);
        photo.setUrl("/uploads/" + filename);
        photo.setCaption(caption);
        photo.setCreatedAt(LocalDateTime.now());

        photoMapper.insert(photo);

        return new PhotoResponse(
                photo.getId(),
                photo.getUrl(),
                photo.getCaption(),
                photo.getUploaderId(),
                null,
                photo.getCreatedAt()
        );
    }

    @Override
    public IPage<PhotoResponse> list(Long coupleId, int page, int size) {
        Page<?> pageParam = new Page<>(page, size);
        return photoMapper.selectPhotoPage(pageParam, coupleId);
    }

    @Override
    @Transactional
    public void delete(Long photoId, Long coupleId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null) {
            throw new BusinessException(404, "Photo not found");
        }
        if (!photo.getCoupleId().equals(coupleId)) {
            throw new BusinessException(403, "Access denied: photo does not belong to this couple");
        }

        // Delete file from disk
        String filename = photo.getUrl().replace("/uploads/", "");
        try {
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BusinessException(500, "Failed to delete file");
        }

        photoMapper.deleteById(photoId);
    }
}
