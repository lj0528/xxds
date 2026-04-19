package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmitAnswerDTO {
    private String testId;
    private List<String> answers;
}