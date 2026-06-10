package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.chat.dto.AnomalyInfo;
import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MockLlmServiceImpl implements LlmService {

    private final AnomalyRuleEngine anomalyRuleEngine;

    @Override
    public ChatResponse chat(ChatRequest request) {
        String message = request.getMessage();
        String context = request.getContext();

        if (message != null && message.contains("异常")) {
            List<AnomalyInfo> anomalies = anomalyRuleEngine.analyze(context);
            String moduleName = getModuleName(context);
            if (anomalies.isEmpty()) {
                return ChatResponse.builder()
                        .reply("已分析" + moduleName + "数据，未发现异常 ✅")
                        .anomalies(List.of())
                        .imagePlaceholder(true)
                        .build();
            }
            return ChatResponse.builder()
                    .reply("已分析" + moduleName + "数据，发现 " + anomalies.size() + " 条异常：")
                    .anomalies(anomalies)
                    .imagePlaceholder(true)
                    .build();
        }

        if (message != null && message.contains("概览")) {
            return ChatResponse.builder()
                    .reply("📊 " + getModuleName(context) + "数据概览（Mock 模式）：\n"
                            + "当前为规则引擎模拟数据，接入真实 AI 后可获得详细统计分析。")
                    .anomalies(List.of())
                    .imagePlaceholder(false)
                    .build();
        }

        return ChatResponse.builder()
                .reply("🤖 我是 AI 助手（当前为 Mock 模式）。\n"
                        + "你可以试试：\n"
                        + "• 点击「📊 数据概览」查看统计\n"
                        + "• 点击「🔍 异常详情」检测数据异常\n"
                        + "• 或在输入框中自由提问")
                .anomalies(List.of())
                .imagePlaceholder(false)
                .build();
    }

    private String getModuleName(String context) {
        if (context == null) return "系统";
        return switch (context) {
            case "product" -> "商品";
            case "user" -> "用户";
            case "article" -> "文章";
            default -> context;
        };
    }
}
