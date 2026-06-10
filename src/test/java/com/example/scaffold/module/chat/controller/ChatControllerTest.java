package com.example.scaffold.module.chat.controller;

import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;
import com.example.scaffold.module.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ChatController 测试")
class ChatControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService = mock(ChatService.class);

    @BeforeEach
    void setUp() {
        ChatController controller = new ChatController(chatService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("正常请求返回 200 和 ChatResponse")
    void shouldReturnChatResponse() throws Exception {
        ChatResponse mockResp = ChatResponse.builder()
                .reply("已分析商品数据，发现 1 条异常：")
                .anomalies(List.of())
                .imagePlaceholder(true)
                .build();
        when(chatService.chat(any())).thenReturn(mockResp);

        ChatRequest req = new ChatRequest();
        req.setMessage("罗列商品的异常情况");
        req.setContext("product");

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reply").exists());
    }

    @Test
    @DisplayName("无 context 返回正常处理")
    void shouldHandleNullContext() throws Exception {
        ChatResponse mockResp = ChatResponse.builder()
                .reply("已分析系统数据，未发现异常")
                .anomalies(List.of())
                .imagePlaceholder(false)
                .build();
        when(chatService.chat(any())).thenReturn(mockResp);

        ChatRequest req = new ChatRequest();
        req.setMessage("数据概览");
        req.setContext(null);

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("Service 返回异常列表时正确序列化")
    void shouldSerializeAnomalies() throws Exception {
        ChatResponse mockResp = ChatResponse.builder()
                .reply("发现 2 条异常")
                .anomalies(List.of(
                        com.example.scaffold.module.chat.dto.AnomalyInfo.builder()
                                .type("价格异常").field("price")
                                .actualValue("-99").expectedValue("> 0")
                                .severity("严重").recordId(1L).recordName("商品A")
                                .suggestion("价格不能为负数").build(),
                        com.example.scaffold.module.chat.dto.AnomalyInfo.builder()
                                .type("库存不足").field("stock")
                                .actualValue("-5").expectedValue("≥ 0")
                                .severity("警告").recordId(2L).recordName("商品B")
                                .suggestion("库存不能为负数").build()
                ))
                .imagePlaceholder(true)
                .build();
        when(chatService.chat(any())).thenReturn(mockResp);

        ChatRequest req = new ChatRequest();
        req.setMessage("异常");
        req.setContext("product");

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anomalies.length()").value(2))
                .andExpect(jsonPath("$.data.anomalies[0].type").value("价格异常"))
                .andExpect(jsonPath("$.data.imagePlaceholder").value(true));
    }
}
