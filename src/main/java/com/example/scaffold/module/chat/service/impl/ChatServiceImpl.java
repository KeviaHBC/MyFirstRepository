package com.example.scaffold.module.chat.service.impl;

import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;
import com.example.scaffold.module.chat.llm.LlmService;
import com.example.scaffold.module.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final LlmService llmService;

    @Override
    public ChatResponse chat(ChatRequest request) {
        return llmService.chat(request);
    }
}
