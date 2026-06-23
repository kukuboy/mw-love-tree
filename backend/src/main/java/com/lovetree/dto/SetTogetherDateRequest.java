package com.lovetree.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SetTogetherDateRequest {
    @NotNull
    private LocalDate togetherDate;
}
