package com.linalg.linear_algebra_assistant.controller;

import com.linalg.linear_algebra_assistant.entity.Theorem;
import com.linalg.linear_algebra_assistant.service.TheoremService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theorems")
@RequiredArgsConstructor
public class TheoremController {
    private final TheoremService theoremService;

    @GetMapping("/chapter/{chapterId}")
    public List<Theorem> getByChapter(@PathVariable Integer chapterId) {
        return theoremService.getTheoremsByChapter(chapterId);
    }

    @GetMapping("/search")
    public List<Theorem> search(@RequestParam(required = false) String keyword) {
        return theoremService.searchTheorems(keyword);
    }

    @GetMapping("/{id}")
    public Theorem getById(@PathVariable String id) {
        return theoremService.getTheoremById(id);
    }
}