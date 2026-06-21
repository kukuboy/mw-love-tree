package com.lovetree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeResponse {
    private String stage;
    private Integer leafCount;
    private Integer flowerCount;
    private String stageLabel;
}
