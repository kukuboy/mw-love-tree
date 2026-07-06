package com.lovetree.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lovetree.common.BusinessException;
import com.lovetree.common.JwtUtil;
import com.lovetree.dto.AuthResponse;
import com.lovetree.entity.Couple;
import com.lovetree.entity.User;
import com.lovetree.mapper.CoupleMapper;
import com.lovetree.mapper.UserMapper;
import com.lovetree.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png", "gif", "webp");

    private final UserMapper userMapper;
    private final CoupleMapper coupleMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${lovetree.upload.dir}")
    private String uploadDir;

    public AuthServiceImpl(UserMapper userMapper, CoupleMapper coupleMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.coupleMapper = coupleMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public AuthResponse register(String email, String password, String nickname) {
        // Check email uniqueness
        User existing = userMapper.findByEmail(email);
        if (existing != null) {
            throw new BusinessException(409, "Email already registered");
        }

        // Hash password and insert user
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setNickname(nickname);
        userMapper.insert(user);

        // Generate JWT (coupleId = null on registration)
        String token = jwtUtil.generateToken(user.getId(), null);

        return new AuthResponse(token, user.getId(), user.getNickname(), null, null);
    }

    @Override
    public AuthResponse login(String email, String password) {
        // Find user by email
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException(401, "Invalid credentials");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "Invalid credentials");
        }

        // Find user's couple (if any)
        QueryWrapper<Couple> qw = new QueryWrapper<>();
        qw.eq("user1_id", user.getId()).or().eq("user2_id", user.getId());
        Couple couple = coupleMapper.selectOne(qw);
        Long coupleId = couple != null ? couple.getId() : null;

        // Generate JWT
        String token = jwtUtil.generateToken(user.getId(), coupleId);

        return new AuthResponse(token, user.getId(), user.getNickname(), coupleId, user.getAvatar());
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }

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

        // Delete previous avatar file if it exists
        if (user.getAvatar() != null && user.getAvatar().startsWith("/uploads/")) {
            String oldFilename = user.getAvatar().replace("/uploads/", "");
            try {
                Files.deleteIfExists(Paths.get(uploadDir, oldFilename));
            } catch (IOException ignored) {
                // Best effort cleanup; ignore failure
            }
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path targetPath = Paths.get(uploadDir, filename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "Failed to save file");
        }

        String avatarUrl = "/uploads/" + filename;
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);

        return avatarUrl;
    }
}
