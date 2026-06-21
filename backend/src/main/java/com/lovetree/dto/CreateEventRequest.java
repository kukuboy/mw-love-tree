package com.lovetree.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateEventRequest {
    @NotBlank
    private String title;

    private String content;

    @NotBlank
    private String eventType;

    @NotNull
    private LocalDate eventDate;

    private List<String> images;

    private String mood;
}
