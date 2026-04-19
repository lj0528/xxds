package com.linalg.linear_algebra_assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("test_record")
public class TestRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private Long userId;
    private String testType;
    private Integer chapterId;
    private BigDecimal score;
    private BigDecimal totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
