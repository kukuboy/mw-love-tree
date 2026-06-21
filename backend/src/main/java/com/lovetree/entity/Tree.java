package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("tree")
public class Tree {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private String stage;
    private Integer leafCount;
    private Integer flowerCount;
}
