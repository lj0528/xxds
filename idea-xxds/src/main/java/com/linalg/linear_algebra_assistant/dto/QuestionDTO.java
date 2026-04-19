package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;

@Data
public class QuestionDTO {
    private String id;
    private String type;
    private String content;
    private Boolean hasMatrix;
}
