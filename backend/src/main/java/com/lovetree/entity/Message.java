package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long fromId;
    private Long toId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
