package com.linalg.linear_algebra_assistant.service.impl;

import com.linalg.linear_algebra_assistant.entity.Theorem;
import com.linalg.linear_algebra_assistant.mapper.TheoremMapper;
import com.linalg.linear_algebra_assistant.service.TheoremService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TheoremServiceImpl implements TheoremService {
    private final TheoremMapper theoremMapper;

    @Override
    public List<Theorem> getTheoremsByChapter(Integer chapterId) {
        return theoremMapper.selectByChapter(chapterId);
    }

    @Override
    public List<Theorem> searchTheorems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return theoremMapper.selectList(null);
        }
        return theoremMapper.searchByKeyword(keyword);
    }

    @Override
    public Theorem getTheoremById(String id) {
        return theoremMapper.selectById(id);
    }
}
