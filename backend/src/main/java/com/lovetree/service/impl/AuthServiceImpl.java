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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final CoupleMapper coupleMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

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

        return new AuthResponse(token, user.getId(), user.getNickname(), null);
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

        return new AuthResponse(token, user.getId(), user.getNickname(), coupleId);
    }
}
