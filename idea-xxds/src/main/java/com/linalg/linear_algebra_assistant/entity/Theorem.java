package com.linalg.linear_algebra_assistant.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("theorem")
public class Theorem {
    @TableId
    private String id;
    private Integer chapterId;
    private String name;
    private String content;
    private String proof;
    private String example;
    private String relatedTheorems;
    private Integer difficulty;
}