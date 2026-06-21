package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("photo")
public class Photo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long uploaderId;
    private String url;
    private String caption;
    private LocalDateTime createdAt;
}
