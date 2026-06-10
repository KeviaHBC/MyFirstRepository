package com.example.scaffold.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    /** 用户消息 */
    @NotBlank(message = "消息不能为空")
    private String message;

    /** 模块上下文: product | user | article */
    private String context;
}
