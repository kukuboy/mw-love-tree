package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeStatsResponse {
    private Long totalEvents;
    private Map<String, Integer> typeDistribution;
    private Map<String, Integer> monthlyCounts;
}
