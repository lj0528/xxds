package com.linalg.linear_algebra_assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String type;
    private Integer difficulty;
    private Integer chapterId;
    private String content;
    private String answer;
    private String solution;
    private Boolean hasMatrix;
    //private String knowledgePoints;
}