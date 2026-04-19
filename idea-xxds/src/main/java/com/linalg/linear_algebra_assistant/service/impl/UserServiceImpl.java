package com.linalg.linear_algebra_assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linalg.linear_algebra_assistant.dto.LearningReportDTO;
import com.linalg.linear_algebra_assistant.dto.WeakChapterDTO;
import com.linalg.linear_algebra_assistant.dto.WrongQuestionDTO;
import com.linalg.linear_algebra_assistant.entity.Question;
import com.linalg.linear_algebra_assistant.entity.TestRecord;
import com.linalg.linear_algebra_assistant.entity.User;
import com.linalg.linear_algebra_assistant.entity.UserAnswer;
import com.linalg.linear_algebra_assistant.mapper.*;
import com.linalg.linear_algebra_assistant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final TestRecordMapper testRecordMapper;
    private final UserAnswerMapper userAnswerMapper;
    private final QuestionMapper questionMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public boolean register(User user) {
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("student");
        user.setCreatedAt(LocalDateTime.now());
        return userMapper.insert(user) > 0;
    }

    @Override
    public LearningReportDTO getLearningReport(Long userId) {
        // 1. 查询用户所有测试记录
        LambdaQueryWrapper<TestRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(TestRecord::getUserId, userId);
        List<TestRecord> records = testRecordMapper.selectList(recordWrapper);

        if (records.isEmpty()) {
            LearningReportDTO empty = new LearningReportDTO();
            empty.setTotalTests(0);
            empty.setAvgScore(0.0);
            empty.setWeakChapters(Collections.emptyList());
            return empty;
        }

        int totalTests = records.size();
        double totalScore = records.stream()
                .mapToDouble(r -> r.getScore() == null ? 0 : r.getScore().doubleValue())
                .sum();
        double avgScore = totalScore / totalTests;

        // 2. 获取用户所有答题记录
        LambdaQueryWrapper<UserAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(UserAnswer::getUserId, userId);
        List<UserAnswer> answers = userAnswerMapper.selectList(answerWrapper);

        // 3. 获取所有题目（用于映射章节）
        List<Question> allQuestions = questionMapper.selectList(null);
        Map<String, Integer> questionChapterMap = allQuestions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getChapterId));

        // 4. 按章节分组统计正确率
        Map<Integer, List<UserAnswer>> chapterAnswers = new HashMap<>();
        for (UserAnswer ans : answers) {
            Integer chapterId = questionChapterMap.get(ans.getQuestionId());
            if (chapterId != null) {
                chapterAnswers.computeIfAbsent(chapterId, k -> new ArrayList<>()).add(ans);
            }
        }

        List<WeakChapterDTO> weakChapters = new ArrayList<>();
        for (Map.Entry<Integer, List<UserAnswer>> entry : chapterAnswers.entrySet()) {
            int chapterId = entry.getKey();
            List<UserAnswer> ansList = entry.getValue();
            long correctCount = ansList.stream()
                    .filter(a -> a.getScore() != null && a.getScore().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            double correctRate = ansList.isEmpty() ? 0 : (correctCount * 100.0 / ansList.size());
            if (correctRate < 60) {
                WeakChapterDTO weak = new WeakChapterDTO();
                weak.setChapterId(chapterId);
                weak.setCorrectRate(Math.round(correctRate * 10) / 10.0);
                weakChapters.add(weak);
            }
        }

        LearningReportDTO dto = new LearningReportDTO();
        dto.setTotalTests(totalTests);
        dto.setAvgScore(Math.round(avgScore * 10) / 10.0);
        dto.setWeakChapters(weakChapters);
        return dto;
    }

    @Override
    public List<WrongQuestionDTO> getWrongQuestions(Long userId) {
        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getUserId, userId)
                .le(UserAnswer::getScore, BigDecimal.ZERO);
        List<UserAnswer> wrongAnswers = userAnswerMapper.selectList(wrapper);

        if (wrongAnswers.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> questionIds = wrongAnswers.stream()
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<String, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<WrongQuestionDTO> result = new ArrayList<>();
        for (UserAnswer ans : wrongAnswers) {
            Question q = questionMap.get(ans.getQuestionId());
            if (q == null) continue;
            WrongQuestionDTO dto = new WrongQuestionDTO();
            dto.setId(q.getId());
            dto.setContent(q.getContent());
            dto.setUserAnswer(ans.getUserAnswer());
            dto.setCorrectAnswer(q.getAnswer());
            dto.setSolution(q.getSolution());
            result.add(dto);
        }
        return result;
    }

    // 新增：删除错题
    @Override
    @Transactional("transactionManager")
    public void deleteWrongQuestion(Long userId, String questionId) {
        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getUserId, userId)
                .eq(UserAnswer::getQuestionId, questionId);
        userAnswerMapper.delete(wrapper);
    }
}