package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;

@Data
public class WeakChapterDTO {
    private Integer chapterId;
    private Double correctRate;  // 正确率百分比
}
