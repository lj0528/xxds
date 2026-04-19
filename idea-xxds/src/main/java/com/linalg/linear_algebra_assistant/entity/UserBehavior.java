package com.linalg.linear_algebra_assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String questionId;
    private Integer chapterId;
    private Integer isCorrect;
    private BigDecimal score;
    private Integer timeSpent;
    private String behaviorType;
    private LocalDateTime createdAt;
}