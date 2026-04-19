package com.linalg.linear_algebra_assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linalg.linear_algebra_assistant.dto.*;
import com.linalg.linear_algebra_assistant.entity.Question;
import com.linalg.linear_algebra_assistant.entity.TestRecord;
import com.linalg.linear_algebra_assistant.entity.UserAnswer;
import com.linalg.linear_algebra_assistant.mapper.QuestionMapper;
import com.linalg.linear_algebra_assistant.mapper.TestRecordMapper;
import com.linalg.linear_algebra_assistant.mapper.UserAnswerMapper;
import com.linalg.linear_algebra_assistant.service.TestService;
import com.linalg.linear_algebra_assistant.mapper.UserBehaviorMapper;
import com.linalg.linear_algebra_assistant.entity.UserBehavior;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final QuestionMapper questionMapper;
    private final TestRecordMapper testRecordMapper;
    private final UserAnswerMapper userAnswerMapper;
    private final UserBehaviorMapper userBehaviorMapper;

    // 内存缓存：临时试卷ID -> 题目ID列表（用于随机抽题后提交时匹配）
    private final Map<String, List<String>> tempTestCache = new ConcurrentHashMap<>();

    @Override
    public TestPaperDTO getTestPaper(String testId) {
        // 原有方法保持不变（根据ID获取试卷，此处仍返回所有题目，可保留）
        List<Question> questions = questionMapper.selectList(null);
        return convertToPaper(questions, testId);
    }
    /*
    @Override
    public TestPaperDTO getRandomTest(int count) {
        return getRandomTest(count, null);  // 难度为 null 表示全随机
    }*/

    @Override
    public TestPaperDTO getRandomTest(int count, Integer difficulty) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        List<Question> allQuestions = questionMapper.selectList(wrapper);
        if (allQuestions.size() < count) {
            count = allQuestions.size();
        }
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.stream().limit(count).collect(Collectors.toList());
        if (selected.size() > count) {
            selected = selected.subList(0, count);
        }

        String tempTestId = UUID.randomUUID().toString();
        List<String> questionIds = selected.stream().map(Question::getId).collect(Collectors.toList());
        tempTestCache.put(tempTestId, questionIds);

        return convertToPaper(selected, tempTestId);
    }

    @Override
    public TestPaperDTO getRecommendedTest(Long userId, int count) {
        List<Question> recommended = recommendQuestions(userId, count);
        if (recommended.size() < count) {
            // 不足部分随机补齐
            List<Question> all = questionMapper.selectList(null);
            Collections.shuffle(all);
            for (Question q : all) {
                if (!recommended.contains(q)) {
                    recommended.add(q);
                    if (recommended.size() >= count) break;
                }
            }
        }
        // 防御：确保不超过 count
        if (recommended.size() > count) {
            recommended = recommended.subList(0, count);
        }
        String tempTestId = UUID.randomUUID().toString();
        List<String> questionIds = recommended.stream().map(Question::getId).collect(Collectors.toList());
        tempTestCache.put(tempTestId, questionIds);
        return convertToPaper(recommended, tempTestId);
    }

    /**
     * 基于用户的协同过滤推荐（UserCF）
     */
    private List<Question> recommendQuestions(Long userId, int topN) {
        // 1. 获取所有用户行为
        List<UserBehavior> allBehaviors = userBehaviorMapper.selectList(null);
        if (allBehaviors.isEmpty()) {
            List<Question> all = questionMapper.selectList(null);
            Collections.shuffle(all);
            return all.stream().limit(topN).collect(Collectors.toList());
        }

        // 2. 构建用户-题目评分映射
        Map<Long, Map<String, Double>> userItemScore = new HashMap<>();
        for (UserBehavior b : allBehaviors) {
            double score = b.getIsCorrect() == 1 ? 1.0 : 0.0;
            userItemScore.computeIfAbsent(b.getUserId(), k -> new HashMap<>())
                    .put(b.getQuestionId(), score);
        }

        // 3. 获取当前用户的评分
        Map<String, Double> myScores = userItemScore.get(userId);
        if (myScores == null || myScores.isEmpty()) {
            return getHotQuestions(topN);
        }

        // 4. 计算相似度
        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : userItemScore.entrySet()) {
            Long otherId = entry.getKey();
            if (otherId.equals(userId)) continue;
            double sim = pearsonCorrelation(myScores, entry.getValue());
            if (sim > 0) similarities.put(otherId, sim);
        }

        // 5. 取相似度最高的5个邻居
        List<Long> neighbors = similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. 邻居喜欢的题目（排除自己已做过的）
        Set<String> myDone = myScores.keySet();
        Map<String, Double> itemScore = new HashMap<>();
        for (Long neighbor : neighbors) {
            Map<String, Double> neighborScores = userItemScore.get(neighbor);
            double sim = similarities.get(neighbor);
            for (Map.Entry<String, Double> entry : neighborScores.entrySet()) {
                String qid = entry.getKey();
                if (myDone.contains(qid)) continue;
                double score = entry.getValue();
                if (score > 0) {
                    itemScore.put(qid, itemScore.getOrDefault(qid, 0.0) + sim * score);
                }
            }
        }

        // 7. 排序取前 topN
        List<String> recommendedIds = itemScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (recommendedIds.isEmpty()) {
            return getHotQuestions(topN);
        }

        List<Question> result = new ArrayList<>();
        for (String qid : recommendedIds) {
            Question q = questionMapper.selectById(qid);
            if (q != null) result.add(q);
        }
        return result;
    }

    /**
     * 皮尔逊相关系数计算
     */
    private double pearsonCorrelation(Map<String, Double> x, Map<String, Double> y) {
        Set<String> common = new HashSet<>(x.keySet());
        common.retainAll(y.keySet());
        if (common.size() < 2) return 0;
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();
        for (String key : common) {
            xList.add(x.get(key));
            yList.add(y.get(key));
        }
        double[] xArray = xList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yArray = yList.stream().mapToDouble(Double::doubleValue).toArray();
        org.apache.commons.math3.stat.correlation.PearsonsCorrelation pc = new org.apache.commons.math3.stat.correlation.PearsonsCorrelation();
        return pc.correlation(xArray, yArray);
    }

    /**
     * 获取热门题目（按被做次数排序）
     */
    private List<Question> getHotQuestions(int topN) {
        List<UserBehavior> all = userBehaviorMapper.selectList(null);
        if (all.isEmpty()) {
            List<Question> allQ = questionMapper.selectList(null);
            Collections.shuffle(allQ);
            return allQ.stream().limit(topN).collect(Collectors.toList());
        }
        Map<String, Long> countMap = all.stream()
                .collect(Collectors.groupingBy(UserBehavior::getQuestionId, Collectors.counting()));
        List<String> hotIds = countMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<Question> result = new ArrayList<>();
        for (String qid : hotIds) {
            Question q = questionMapper.selectById(qid);
            if (q != null) result.add(q);
        }
        if (result.size() < topN) {
            List<Question> allQ = questionMapper.selectList(null);
            Collections.shuffle(allQ);
            for (Question q : allQ) {
                if (!result.contains(q)) {
                    result.add(q);
                    if (result.size() >= topN) break;
                }
            }
        }
        return result;
    }

    // 辅助方法：将 Question 列表转换为 TestPaperDTO
    private TestPaperDTO convertToPaper(List<Question> questions, String testId) {
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (Question q : questions) {
            QuestionDTO dto = new QuestionDTO();
            dto.setId(q.getId());
            dto.setType(q.getType());
            dto.setContent(q.getContent());
            dto.setHasMatrix(q.getHasMatrix());
            questionDTOs.add(dto);
        }
        TestPaperDTO paper = new TestPaperDTO();
        paper.setTestId(testId);
        paper.setQuestions(questionDTOs);
        return paper;
    }

    @Override
    @Transactional("transactionManager")
    public TestResultDTO submitTest(SubmitAnswerDTO submitDto, Long userId) {
        String testId = submitDto.getTestId();

        // 1. 根据 testId 获取题目ID列表（优先从随机抽题缓存中获取，如果没有则查询所有题目）
        List<String> questionIds;
        if (tempTestCache.containsKey(testId)) {
            questionIds = tempTestCache.get(testId);
            // 提交后可以移除缓存，防止重复提交（可选）
            tempTestCache.remove(testId);
        } else {
            // 兼容旧的直接试卷ID方式：查询所有题目
            List<Question> allQuestions = questionMapper.selectList(null);
            questionIds = allQuestions.stream().map(Question::getId).collect(Collectors.toList());
        }

        // 2. 根据题目ID列表查询对应的题目（保持顺序）
        List<Question> questions = new ArrayList<>();
        for (String qid : questionIds) {
            Question q = questionMapper.selectById(qid);
            if (q != null) questions.add(q);
        }

        // 3. 创建测试记录
        TestRecord record = new TestRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setTestType(tempTestCache.containsKey(testId) ? "RANDOM" : "COMPREHENSIVE");
        record.setStartTime(LocalDateTime.now().minusMinutes(30));
        record.setEndTime(LocalDateTime.now());
        record.setTotalScore(BigDecimal.valueOf(questions.size() * 10));
        testRecordMapper.insert(record);

        // 4. 逐题评分并保存答案
        List<TestResultDTO.QuestionResultDTO> resultList = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;

        // 注意：前端传递的 answers 顺序应与 questions 顺序一致（按照题目在试卷中的顺序）
        List<String> userAnswers = submitDto.getAnswers();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String userAnswer = (i < userAnswers.size()) ? userAnswers.get(i) : "";
            BigDecimal score = gradeQuestion(q, userAnswer);

            UserAnswer ua = new UserAnswer();
            ua.setTestRecordId(record.getId());
            ua.setQuestionId(q.getId());
            ua.setUserAnswer(userAnswer);
            ua.setScore(score);
            ua.setUserId(userId);
            userAnswerMapper.insert(ua);

            // 记录用户行为
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setQuestionId(q.getId());
            behavior.setChapterId(q.getChapterId());
            behavior.setIsCorrect(score.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
            behavior.setScore(score);
            behavior.setBehaviorType("TEST");
            behavior.setCreatedAt(LocalDateTime.now());
            userBehaviorMapper.insert(behavior);

            TestResultDTO.QuestionResultDTO qr = new TestResultDTO.QuestionResultDTO();
            qr.setQuestionId(q.getId());
            qr.setScore(score);
            qr.setCorrectAnswer(q.getAnswer());
            qr.setSolution(q.getSolution());
            resultList.add(qr);

            totalScore = totalScore.add(score);
        }

        record.setScore(totalScore);
        testRecordMapper.updateById(record);

        TestResultDTO result = new TestResultDTO();
        result.setTestRecordId(record.getId());
        result.setTotalScore(totalScore);
        result.setQuestionResults(resultList);
        return result;
    }

    private BigDecimal gradeQuestion(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) return BigDecimal.ZERO;
        String correct = question.getAnswer();
        if (correct == null) return BigDecimal.ZERO;

        if ("CALCULATION".equals(question.getType())) {
            double userVal = evaluateExpression(userAnswer);
            double correctVal = evaluateExpression(correct);
            if (!Double.isNaN(userVal) && !Double.isNaN(correctVal) && Math.abs(userVal - correctVal) < 1e-6) {
                return BigDecimal.TEN;
            }
            return BigDecimal.ZERO;
        } else {
            return userAnswer.trim().equalsIgnoreCase(correct.trim()) ? BigDecimal.TEN : BigDecimal.ZERO;
        }
    }

    private double evaluateExpression(String expr) {
        try {
            // 清理表达式：去除空格，替换中文乘除号
            String cleaned = expr.trim()
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace(" ", "");
            Expression expression = new ExpressionBuilder(cleaned).build();
            double result = expression.evaluate();
            // 避免浮点误差，保留6位小数
            return Math.round(result * 1e6) / 1e6;
        } catch (Exception e) {
            return Double.NaN;
        }
    }
}