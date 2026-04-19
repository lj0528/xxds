package com.linalg.linear_algebra_assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatOptions;   // 改用 DeepSeekChatOptions
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiAssistantService {

    private final ChatClient chatClient;

    public AiAssistantService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        你是一个专业的线性代数学习助手。
                        能够生动形象的解释回答问题，同时还会举一反三
                        """)
                .defaultOptions(DeepSeekChatOptions.builder()
                        .temperature(0.7)           // 注意：没有 with 前缀
                        .model("deepseek-chat")     // 可选 "deepseek-reasoner"
                        .build())
                .build();
    }

    public Flux<String> streamChat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}