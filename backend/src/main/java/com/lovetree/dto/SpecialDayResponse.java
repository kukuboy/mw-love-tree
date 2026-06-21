package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SpecialDayResponse {
    private String name;
    private LocalDate date;
    private int daysUntil;
    private long daysTogether;
}
