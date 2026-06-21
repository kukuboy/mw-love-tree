package com.lovetree.service;

import com.lovetree.dto.CoupleInfoResponse;

public interface CoupleService {
    String generateInvite(Long userId);
    String join(Long userId, String inviteCode);
    CoupleInfoResponse getInfo(Long coupleId, Long userId);
}
