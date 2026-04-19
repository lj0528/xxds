package com.linalg.linear_algebra_assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableField;

@Data
@TableName("user_answer")
public class UserAnswer {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String testRecordId;
    private String questionId;
    @TableField("answer")
    private String userAnswer;
    private BigDecimal score;
}