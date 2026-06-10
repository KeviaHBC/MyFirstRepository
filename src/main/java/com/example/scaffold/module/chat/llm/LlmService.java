package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;

/**
 * LLM 调用统一接口。
 * 当前 Mock 实现：MockLlmServiceImpl
 * 后续可替换为：OpenAiLlmServiceImpl / ClaudeLlmServiceImpl
 */
public interface LlmService {
    ChatResponse chat(ChatRequest request);
}
