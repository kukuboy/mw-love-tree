package com.lovetree.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email @NotBlank
    private String email;
    @NotBlank @Size(min = 6, max = 64)
    private String password;
    @NotBlank @Size(max = 32)
    private String nickname;
}
