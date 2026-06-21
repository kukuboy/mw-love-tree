package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("couple")
public class Couple {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String inviteCode;
    private LocalDate anniversary;
    private LocalDateTime createdAt;
}
