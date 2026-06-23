package com.lovetree.service;

import com.lovetree.dto.CoupleInfoResponse;

import java.time.LocalDate;

public interface CoupleService {
    String generateInvite(Long userId);
    String join(Long userId, String inviteCode);
    CoupleInfoResponse getInfo(Long coupleId, Long userId);
    void setTogetherDate(Long coupleId, Long userId, LocalDate togetherDate);
}
