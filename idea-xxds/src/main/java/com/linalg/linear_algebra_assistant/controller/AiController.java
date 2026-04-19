package com.linalg.linear_algebra_assistant.controller;

import com.linalg.linear_algebra_assistant.service.AiAssistantService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping(value = "/chat", produces = "text/event-stream")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        return aiAssistantService.streamChat(request.getMessage());
    }

    @PostMapping("/advice")
    public String advice(@RequestBody AdviceRequest request) {
        String prompt = "请根据以下学习报告给出学习建议：\n" + request.getReport();
        return aiAssistantService.chat(prompt);
    }

    // 内部 DTO
    public static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class AdviceRequest {
        private String report;
        public String getReport() { return report; }
        public void setReport(String report) { this.report = report; }
    }
}