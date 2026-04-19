package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;
import java.util.List;

@Data
public class LearningReportDTO {
    private Integer totalTests;
    private Double avgScore;
    private List<WeakChapterDTO> weakChapters;
}