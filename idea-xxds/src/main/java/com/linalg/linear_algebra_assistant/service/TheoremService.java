package com.linalg.linear_algebra_assistant.service;

import com.linalg.linear_algebra_assistant.entity.Theorem;
import java.util.List;

public interface TheoremService {
    List<Theorem> getTheoremsByChapter(Integer chapterId);
    List<Theorem> searchTheorems(String keyword);
    Theorem getTheoremById(String id);
}