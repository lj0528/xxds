package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;

@Data
public class WrongQuestionDTO {
    private String id;
    private String content;
    private String userAnswer;
    private String correctAnswer;
    private String solution;
}