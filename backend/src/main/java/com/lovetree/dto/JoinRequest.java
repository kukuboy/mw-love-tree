package com.lovetree.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRequest {
    @NotBlank
    private String inviteCode;
}
