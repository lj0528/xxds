package com.linalg.linear_algebra_assistant.service;

import com.linalg.linear_algebra_assistant.entity.User;
import com.linalg.linear_algebra_assistant.dto.LearningReportDTO;
import com.linalg.linear_algebra_assistant.dto.WrongQuestionDTO;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    boolean register(User user);
    LearningReportDTO getLearningReport(Long userId);
    List<WrongQuestionDTO> getWrongQuestions(Long userId);
    void deleteWrongQuestion(Long userId, String questionId);
}