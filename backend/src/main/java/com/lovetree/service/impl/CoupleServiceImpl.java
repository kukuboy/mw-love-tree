package com.lovetree.service.impl;

import com.lovetree.common.BusinessException;
import com.lovetree.common.JwtUtil;
import com.lovetree.dto.CoupleInfoResponse;
import com.lovetree.entity.Couple;
import com.lovetree.entity.Tree;
import com.lovetree.entity.User;
import com.lovetree.mapper.CoupleMapper;
import com.lovetree.mapper.TreeMapper;
import com.lovetree.mapper.UserMapper;
import com.lovetree.service.CoupleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class CoupleServiceImpl implements CoupleService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CoupleMapper coupleMapper;
    private final TreeMapper treeMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public CoupleServiceImpl(CoupleMapper coupleMapper, TreeMapper treeMapper, UserMapper userMapper, JwtUtil jwtUtil) {
        this.coupleMapper = coupleMapper;
        this.treeMapper = treeMapper;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public String generateInvite(Long userId) {
        // Check user not already in a couple
        Couple existing = coupleMapper.findByUserId(userId);
        if (existing != null) {
            throw new BusinessException(409, "User is already in a couple");
        }

        // Generate random 8-char alphanumeric code
        String code = generateCode();

        // Insert couple record
        Couple couple = new Couple();
        couple.setUser1Id(userId);
        couple.setInviteCode(code);
        coupleMapper.insert(couple);

        // Insert tree record with stage='SEED'
        Tree tree = new Tree();
        tree.setCoupleId(couple.getId());
        tree.setStage("SEED");
        tree.setLeafCount(0);
        tree.setFlowerCount(0);
        treeMapper.insert(tree);

        return code;
    }

    @Override
    @Transactional
    public String join(Long userId, String inviteCode) {
        // Find couple by invite code
        Couple couple = coupleMapper.findByInviteCode(inviteCode);
        if (couple == null) {
            throw new BusinessException(404, "Invite code not found");
        }

        // Verify joiner is not the creator
        if (couple.getUser1Id().equals(userId)) {
            throw new BusinessException(400, "Cannot pair with yourself");
        }

        // Verify user2_id is null (not already paired)
        if (couple.getUser2Id() != null) {
            throw new BusinessException(409, "Couple is already paired");
        }

        // Set user2_id and anniversary
        couple.setUser2Id(userId);
        couple.setAnniversary(LocalDate.now());
        coupleMapper.updateById(couple);

        // Generate a fresh JWT that includes the coupleId
        return jwtUtil.generateToken(userId, couple.getId());
    }

    @Override
    public CoupleInfoResponse getInfo(Long coupleId, Long userId) {
        // Find couple
        Couple couple = coupleMapper.selectById(coupleId);
        if (couple == null) {
            throw new BusinessException(404, "Couple not found");
        }

        // Determine partner ID
        Long partnerId = couple.getUser1Id().equals(userId)
                ? couple.getUser2Id()
                : couple.getUser1Id();

        // Find partner user
        User partner = partnerId != null ? userMapper.selectById(partnerId) : null;

        // Compute days together
        long daysTogether = 0;
        if (couple.getAnniversary() != null) {
            daysTogether = ChronoUnit.DAYS.between(couple.getAnniversary(), LocalDate.now());
        }

        return new CoupleInfoResponse(
                partner != null ? partner.getNickname() : null,
                partner != null ? partner.getAvatar() : null,
                couple.getInviteCode(),
                couple.getAnniversary(),
                daysTogether
        );
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
