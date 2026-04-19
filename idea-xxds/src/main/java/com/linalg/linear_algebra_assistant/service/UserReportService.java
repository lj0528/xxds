package com.linalg.linear_algebra_assistant.service;

import com.linalg.linear_algebra_assistant.dto.LearningReportDTO;
import com.linalg.linear_algebra_assistant.dto.WrongQuestionDTO;
import java.util.List;

public interface UserReportService {
    LearningReportDTO getLearningReport(Long userId);
    List<WrongQuestionDTO> getWrongQuestions(Long userId);
    void deleteWrongQuestion(Long userId, String questionId);
}