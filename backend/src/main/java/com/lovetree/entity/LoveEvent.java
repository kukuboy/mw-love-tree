package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("love_event")
public class LoveEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long authorId;
    private String title;
    private String content;
    private String eventType;
    private LocalDate eventDate;
    private String images;
    private String mood;
    private LocalDateTime createdAt;
}
