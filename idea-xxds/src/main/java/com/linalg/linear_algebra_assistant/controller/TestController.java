package com.linalg.linear_algebra_assistant.controller;

import com.linalg.linear_algebra_assistant.dto.SubmitAnswerDTO;
import com.linalg.linear_algebra_assistant.dto.TestPaperDTO;
import com.linalg.linear_algebra_assistant.dto.TestResultDTO;
import com.linalg.linear_algebra_assistant.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // 原有：根据试卷ID获取试卷
    @GetMapping("/{testId}")
    public TestPaperDTO getTestPaper(@PathVariable String testId) {
        return testService.getTestPaper(testId);
    }

    // 随机抽题（支持难度参数，可选）
    @GetMapping("/random")
    public TestPaperDTO getRandomTest(@RequestParam(defaultValue = "10") int count,
                                      @RequestParam(required = false) Integer difficulty,
                                      @RequestAttribute(value = "userId", required = false) Long userId) {
        // 临时处理：如果 userId 为空，使用默认值 1（实际应从 token 中获取）
        if (userId == null) userId = 1L;
        return testService.getRandomTest(count, difficulty);
    }

    // 智能推荐组卷
    @GetMapping("/recommend")
    public TestPaperDTO getRecommendedTest(@RequestAttribute(value = "userId", required = false) Long userId,
                                           @RequestParam(defaultValue = "10") int count) {
        if (userId == null) userId = 1L;
        return testService.getRecommendedTest(userId, count);
    }

    // 提交试卷
    @PostMapping("/submit")
    public TestResultDTO submitTest(@RequestBody SubmitAnswerDTO submitDto,
                                    @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return testService.submitTest(submitDto, userId);
    }
}