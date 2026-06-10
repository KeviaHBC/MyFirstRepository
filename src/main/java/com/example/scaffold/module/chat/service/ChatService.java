package com.example.scaffold.module.chat.service;

import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;

public interface ChatService {
    ChatResponse chat(ChatRequest request);
}
