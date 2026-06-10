package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.chat.dto.AnomalyInfo;
import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MockLlmServiceImpl 单元测试")
class MockLlmServiceImplTest {

    @Mock private AnomalyRuleEngine anomalyRuleEngine;
    @InjectMocks private MockLlmServiceImpl service;

    @Nested
    @DisplayName("意图路由")
    class IntentRouting {

        @Test
        @DisplayName("包含'异常'关键词 → 调用规则引擎")
        void shouldRouteToAnomalyEngine() {
            AnomalyInfo anomaly = AnomalyInfo.builder()
                    .type("价格异常").severity("严重").build();
            when(anomalyRuleEngine.analyze(eq("product")))
                    .thenReturn(List.of(anomaly));

            ChatRequest req = new ChatRequest();
            req.setMessage("罗列商品的异常情况");
            req.setContext("product");
            ChatResponse resp = service.chat(req);

            assertThat(resp.getAnomalies()).hasSize(1);
            assertThat(resp.isImagePlaceholder()).isTrue();
        }

        @Test
        @DisplayName("包含'概览'关键词 → 返回统计摘要")
        void shouldRouteToOverview() {
            ChatRequest req = new ChatRequest();
            req.setMessage("数据概览");
            req.setContext("product");
            ChatResponse resp = service.chat(req);

            assertThat(resp.getReply()).contains("概览");
            assertThat(resp.getAnomalies()).isEmpty();
        }

        @Test
        @DisplayName("未知消息 → 返回通用 Mock 回复")
        void shouldReturnGenericReply() {
            ChatRequest req = new ChatRequest();
            req.setMessage("你好");
            req.setContext("product");
            ChatResponse resp = service.chat(req);

            assertThat(resp.getReply()).contains("Mock 模式");
            assertThat(resp.getAnomalies()).isEmpty();
            assertThat(resp.isImagePlaceholder()).isFalse();
        }

        @Test
        @DisplayName("无异常时返回'未发现异常'")
        void shouldReportNoAnomalies() {
            when(anomalyRuleEngine.analyze(eq("product")))
                    .thenReturn(List.of());

            ChatRequest req = new ChatRequest();
            req.setMessage("异常检测");
            req.setContext("product");
            ChatResponse resp = service.chat(req);

            assertThat(resp.getReply()).contains("未发现异常");
            assertThat(resp.getAnomalies()).isEmpty();
        }
    }
}
