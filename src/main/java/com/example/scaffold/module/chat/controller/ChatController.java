package com.example.scaffold.module.chat.controller;

import com.example.scaffold.common.result.R;
import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;
import com.example.scaffold.module.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 聊天")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送聊天消息")
    @PostMapping("/send")
    public R<ChatResponse> send(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return R.ok(response);
    }
}
