package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TestResultDTO {
    private String testRecordId;
    private BigDecimal totalScore;
    private List<QuestionResultDTO> questionResults;

    @Data
    public static class QuestionResultDTO {
        private String questionId;
        private BigDecimal score;
        private String correctAnswer;
        private String solution;
    }
}
