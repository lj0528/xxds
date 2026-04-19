package com.linalg.linear_algebra_assistant.dto;

import lombok.Data;
import java.util.List;

@Data
public class TestPaperDTO {
    private String testId;
    private List<QuestionDTO> questions;
}