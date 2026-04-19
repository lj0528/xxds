package com.linalg.linear_algebra_assistant.controller;

import com.linalg.linear_algebra_assistant.dto.LearningReportDTO;
import com.linalg.linear_algebra_assistant.dto.WrongQuestionDTO;
import com.linalg.linear_algebra_assistant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/report")
    public LearningReportDTO getLearningReport(@RequestAttribute Long userId) {
        return userService.getLearningReport(userId);
    }

    @GetMapping("/wrong-questions")
    public List<WrongQuestionDTO> getWrongQuestions(@RequestAttribute Long userId) {
        return userService.getWrongQuestions(userId);
    }

    @DeleteMapping("/wrong-questions/{questionId}")
    public void deleteWrongQuestion(@RequestAttribute(value = "userId", required = false) Long userId,
                                    @PathVariable String questionId) {
        if (userId == null) userId = 1L;
        userService.deleteWrongQuestion(userId, questionId);
    }
}