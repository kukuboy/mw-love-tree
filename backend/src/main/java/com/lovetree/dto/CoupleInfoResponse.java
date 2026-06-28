package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CoupleInfoResponse {
    private Long partnerId;
    private String partnerNickname;
    private String partnerAvatar;
    private String inviteCode;
    private LocalDate anniversary;
    private LocalDate togetherDate;
    private long daysTogether;
}
