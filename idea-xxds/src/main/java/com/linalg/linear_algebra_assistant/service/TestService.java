package com.linalg.linear_algebra_assistant.service;

import com.linalg.linear_algebra_assistant.dto.TestPaperDTO;
import com.linalg.linear_algebra_assistant.dto.TestResultDTO;
import com.linalg.linear_algebra_assistant.dto.SubmitAnswerDTO;

public interface TestService {
    TestPaperDTO getTestPaper(String testId);
    //TestPaperDTO getRandomTest(int count);
    TestResultDTO submitTest(SubmitAnswerDTO submitDto, Long userId);
    // 新增：按难度随机抽题
    TestPaperDTO getRandomTest(int count, Integer difficulty);

    // 新增：智能推荐组卷
    TestPaperDTO getRecommendedTest(Long userId, int count);
}