# AI 聊天助手 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Spring Boot + Thymeleaf + Layui 后台管理系统中添加 AI 聊天助手：右下角悬浮按钮 → 右侧抽屉面板 → 自动上下文感知 → Mock 规则引擎检测异常

**Architecture:** 前端在 index.html 新增抽屉面板 HTML/CSS/JS（复用 Noir Amber 主题变量），后端新增 chat 模块（Controller → Service → LlmService 接口 → MockLlmServiceImpl → AnomalyRuleEngine），通过 LlmService 接口抽象支持后续替换为真实 AI

**Tech Stack:** Spring Boot 3.4.5, Java 21, Maven, MyBatis-Plus 3.5.9, Thymeleaf, Layui 2.9.8, H2, JUnit 5

---

## 文件结构总览

```
新增文件:
  src/main/java/com/example/scaffold/module/chat/
    dto/ChatRequest.java
    dto/ChatResponse.java
    dto/AnomalyInfo.java
    llm/LlmService.java              (接口)
    llm/MockLlmServiceImpl.java
    llm/AnomalyRuleEngine.java
    service/ChatService.java          (接口)
    service/impl/ChatServiceImpl.java
    controller/ChatController.java
  src/main/resources/static/js/chat.js
  src/test/java/com/example/scaffold/module/chat/
    controller/ChatControllerTest.java
    llm/MockLlmServiceImplTest.java
    llm/AnomalyRuleEngineTest.java

修改文件:
  src/main/resources/static/css/app.css    (追加 ~200 行)
  src/main/resources/templates/index.html  (追加 HTML + 引用 chat.js)
```

---

### Task 1: 创建 DTO 类

**Files:**
- Create: `src/main/java/com/example/scaffold/module/chat/dto/ChatRequest.java`
- Create: `src/main/java/com/example/scaffold/module/chat/dto/ChatResponse.java`
- Create: `src/main/java/com/example/scaffold/module/chat/dto/AnomalyInfo.java`

- [ ] **Step 1: 创建 ChatRequest.java**

```java
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
```

- [ ] **Step 2: 创建 AnomalyInfo.java**

```java
package com.example.scaffold.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyInfo {

    /** 异常类型，如"价格异常" */
    private String type;

    /** 涉及字段，如"price" */
    private String field;

    /** 实际值 */
    private String actualValue;

    /** 期望值 */
    private String expectedValue;

    /** 严重程度: 严重 | 警告 | 提示 */
    private String severity;

    /** 关联记录 ID */
    private Long recordId;

    /** 关联记录名称 */
    private String recordName;

    /** 修复建议 */
    private String suggestion;
}
```

- [ ] **Step 3: 创建 ChatResponse.java**

```java
package com.example.scaffold.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /** AI 文本回复 */
    private String reply;

    /** 异常列表（可为空） */
    private List<AnomalyInfo> anomalies;

    /** 是否显示图片占位区 */
    private boolean imagePlaceholder;
}
```

- [ ] **Step 4: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/example/scaffold/module/chat/dto/
git commit -m "feat(chat): add ChatRequest, ChatResponse, AnomalyInfo DTOs

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: 创建 AnomalyRuleEngine 规则引擎

**Files:**
- Create: `src/main/java/com/example/scaffold/module/chat/llm/AnomalyRuleEngine.java`

- [ ] **Step 1: 创建 AnomalyRuleEngine**

```java
package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import com.example.scaffold.module.chat.dto.AnomalyInfo;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 规则引擎：对每个模块的数据逐条应用检测规则，生成异常列表。
 * 规则通过代码配置，可通过 application.yml 扩展。
 */
@Component
@RequiredArgsConstructor
public class AnomalyRuleEngine {

    private final ProductService productService;
    private final UserService userService;
    private final ArticleService articleService;

    /**
     * 根据模块上下文执行异常检测。
     *
     * @param context 模块名: product | user | article
     * @return 异常列表，无异常时返回空列表
     */
    public List<AnomalyInfo> analyze(String context) {
        if (context == null) return List.of();

        return switch (context) {
            case "product" -> analyzeProducts();
            case "user" -> analyzeUsers();
            case "article" -> analyzeArticles();
            default -> List.of();
        };
    }

    // ── 商品异常检测 ──────────────────────────────────────

    private List<AnomalyInfo> analyzeProducts() {
        List<AnomalyInfo> results = new ArrayList<>();
        List<Product> products = productService.list();

        for (Product p : products) {
            // 规则1: 价格异常 (price <= 0)
            if (p.getPrice() != null && p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                results.add(AnomalyInfo.builder()
                        .type("价格异常")
                        .field("price")
                        .actualValue(p.getPrice().toString())
                        .expectedValue("> 0")
                        .severity("严重")
                        .recordId(p.getId())
                        .recordName(p.getName())
                        .suggestion("价格不能为零或负数，请检查数据录入")
                        .build());
            }

            // 规则2: 库存不足 (stock < 0)
            if (p.getStock() != null && p.getStock() < 0) {
                results.add(AnomalyInfo.builder()
                        .type("库存不足")
                        .field("stock")
                        .actualValue(String.valueOf(p.getStock()))
                        .expectedValue("≥ 0")
                        .severity("警告")
                        .recordId(p.getId())
                        .recordName(p.getName())
                        .suggestion("库存数量不能为负数，请核实库存数据")
                        .build());
            }
        }

        return results;
    }

    // ── 用户异常检测 ──────────────────────────────────────

    private List<AnomalyInfo> analyzeUsers() {
        List<AnomalyInfo> results = new ArrayList<>();
        List<User> users = userService.list();

        for (User u : users) {
            // 规则1: 缺少邮箱
            if (u.getEmail() == null || u.getEmail().isBlank()) {
                results.add(AnomalyInfo.builder()
                        .type("缺少邮箱")
                        .field("email")
                        .actualValue("（空）")
                        .expectedValue("有效的邮箱地址")
                        .severity("警告")
                        .recordId(u.getId())
                        .recordName(u.getUsername())
                        .suggestion("请为用户补充邮箱地址，以便接收通知")
                        .build());
            }

            // 规则2: 用户名过长
            if (u.getUsername() != null && u.getUsername().length() > 50) {
                results.add(AnomalyInfo.builder()
                        .type("用户名过长")
                        .field("username")
                        .actualValue(u.getUsername().length() + " 字符")
                        .expectedValue("≤ 50 字符")
                        .severity("提示")
                        .recordId(u.getId())
                        .recordName(u.getUsername())
                        .suggestion("建议缩短用户名至50字符以内")
                        .build());
            }
        }

        return results;
    }

    // ── 文章异常检测 ──────────────────────────────────────

    private List<AnomalyInfo> analyzeArticles() {
        List<AnomalyInfo> results = new ArrayList<>();
        List<Article> articles = articleService.list();

        for (Article a : articles) {
            // 规则1: 标题为空
            if (a.getTitle() == null || a.getTitle().isBlank()) {
                results.add(AnomalyInfo.builder()
                        .type("标题为空")
                        .field("title")
                        .actualValue("（空）")
                        .expectedValue("非空标题")
                        .severity("严重")
                        .recordId(a.getId())
                        .recordName("文章#" + a.getId())
                        .suggestion("文章标题不能为空，请补充标题")
                        .build());
            }
        }

        return results;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/scaffold/module/chat/llm/AnomalyRuleEngine.java
git commit -m "feat(chat): add AnomalyRuleEngine with rules for product/user/article

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: 创建 LlmService 接口和 MockLlmServiceImpl

**Files:**
- Create: `src/main/java/com/example/scaffold/module/chat/llm/LlmService.java`
- Create: `src/main/java/com/example/scaffold/module/chat/llm/MockLlmServiceImpl.java`

- [ ] **Step 1: 创建 LlmService 接口**

```java
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
```

- [ ] **Step 2: 创建 MockLlmServiceImpl（含意图路由）**

```java
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

        // 意图路由：关键词匹配
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

        // 默认通用回复
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
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/scaffold/module/chat/llm/LlmService.java \
        src/main/java/com/example/scaffold/module/chat/llm/MockLlmServiceImpl.java
git commit -m "feat(chat): add LlmService interface and MockLlmServiceImpl with intent routing

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 4: 创建 ChatService 和 ChatServiceImpl

**Files:**
- Create: `src/main/java/com/example/scaffold/module/chat/service/ChatService.java`
- Create: `src/main/java/com/example/scaffold/module/chat/service/impl/ChatServiceImpl.java`

- [ ] **Step 1: 创建 ChatService 接口**

```java
package com.example.scaffold.module.chat.service;

import com.example.scaffold.module.chat.dto.ChatRequest;
import com.example.scaffold.module.chat.dto.ChatResponse;

public interface ChatService {
    ChatResponse chat(ChatRequest request);
}
```

- [ ] **Step 2: 创建 ChatServiceImpl**

```java
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
```

- [ ] **Step 3: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/scaffold/module/chat/service/
git commit -m "feat(chat): add ChatService and ChatServiceImpl

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 5: 创建 ChatController

**Files:**
- Create: `src/main/java/com/example/scaffold/module/chat/controller/ChatController.java`

- [ ] **Step 1: 创建 ChatController**

```java
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
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/scaffold/module/chat/controller/
git commit -m "feat(chat): add ChatController with POST /api/chat/send

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 6: 编写后端测试

**Files:**
- Create: `src/test/java/com/example/scaffold/module/chat/llm/AnomalyRuleEngineTest.java`
- Create: `src/test/java/com/example/scaffold/module/chat/llm/MockLlmServiceImplTest.java`
- Create: `src/test/java/com/example/scaffold/module/chat/controller/ChatControllerTest.java`

- [ ] **Step 1: 编写 AnomalyRuleEngineTest**

```java
package com.example.scaffold.module.chat.llm;

import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import com.example.scaffold.module.chat.dto.AnomalyInfo;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnomalyRuleEngine 单元测试")
class AnomalyRuleEngineTest {

    @Mock private ProductService productService;
    @Mock private UserService userService;
    @Mock private ArticleService articleService;

    @InjectMocks
    private AnomalyRuleEngine engine;

    @Nested
    @DisplayName("商品异常检测")
    class ProductAnalysis {

        @Test
        @DisplayName("价格为负数时检测到价格异常")
        void shouldDetectNegativePrice() {
            Product p = new Product();
            p.setId(1L);
            p.setName("问题商品");
            p.setPrice(new BigDecimal("-99"));
            p.setStock(10);

            when(productService.list()).thenReturn(List.of(p));

            List<AnomalyInfo> results = engine.analyze("product");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("价格异常");
            assertThat(results.get(0).getSeverity()).isEqualTo("严重");
            assertThat(results.get(0).getField()).isEqualTo("price");
        }

        @Test
        @DisplayName("库存为负数时检测到库存不足")
        void shouldDetectNegativeStock() {
            Product p = new Product();
            p.setId(2L);
            p.setName("缺货商品");
            p.setPrice(new BigDecimal("100"));
            p.setStock(-5);

            when(productService.list()).thenReturn(List.of(p));

            List<AnomalyInfo> results = engine.analyze("product");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("库存不足");
            assertThat(results.get(0).getSeverity()).isEqualTo("警告");
        }

        @Test
        @DisplayName("正常商品不产生异常")
        void shouldReturnEmptyForValidProducts() {
            Product p = new Product();
            p.setId(3L);
            p.setName("正常商品");
            p.setPrice(new BigDecimal("99"));
            p.setStock(50);

            when(productService.list()).thenReturn(List.of(p));

            List<AnomalyInfo> results = engine.analyze("product");

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("空商品列表不产生异常")
        void shouldReturnEmptyForEmptyProductList() {
            when(productService.list()).thenReturn(List.of());

            List<AnomalyInfo> results = engine.analyze("product");

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("用户异常检测")
    class UserAnalysis {

        @Test
        @DisplayName("邮箱为空时检测到缺少邮箱")
        void shouldDetectMissingEmail() {
            User u = new User();
            u.setId(1L);
            u.setUsername("testuser");
            u.setEmail(null);

            when(userService.list()).thenReturn(List.of(u));

            List<AnomalyInfo> results = engine.analyze("user");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("缺少邮箱");
            assertThat(results.get(0).getSeverity()).isEqualTo("警告");
        }

        @Test
        @DisplayName("用户名超过50字符时检测到异常")
        void shouldDetectLongUsername() {
            User u = new User();
            u.setId(2L);
            u.setUsername("a".repeat(51));
            u.setEmail("test@example.com");

            when(userService.list()).thenReturn(List.of(u));

            List<AnomalyInfo> results = engine.analyze("user");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("用户名过长");
            assertThat(results.get(0).getSeverity()).isEqualTo("提示");
        }
    }

    @Nested
    @DisplayName("文章异常检测")
    class ArticleAnalysis {

        @Test
        @DisplayName("标题为空时检测到异常")
        void shouldDetectEmptyTitle() {
            Article a = new Article();
            a.setId(1L);
            a.setTitle(null);

            when(articleService.list()).thenReturn(List.of(a));

            List<AnomalyInfo> results = engine.analyze("article");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getType()).isEqualTo("标题为空");
            assertThat(results.get(0).getSeverity()).isEqualTo("严重");
        }
    }

    @Test
    @DisplayName("未知 context 返回空列表")
    void shouldReturnEmptyForUnknownContext() {
        assertThat(engine.analyze("unknown")).isEmpty();
    }

    @Test
    @DisplayName("null context 返回空列表")
    void shouldReturnEmptyForNullContext() {
        assertThat(engine.analyze(null)).isEmpty();
    }
}
```

- [ ] **Step 2: 编写 MockLlmServiceImplTest**

```java
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
```

- [ ] **Step 3: 编写 ChatControllerTest**

```java
package com.example.scaffold.module.chat.controller;

import com.example.scaffold.common.util.JwtUtil;
import com.example.scaffold.module.chat.dto.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ChatController 集成测试")
class ChatControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        token = "Bearer " + jwtUtil.generateToken(1L, "test");
    }

    @Test
    @DisplayName("正常请求返回 200 和 ChatResponse")
    void shouldReturnChatResponse() throws Exception {
        ChatRequest req = new ChatRequest();
        req.setMessage("罗列商品的异常情况");
        req.setContext("product");

        mockMvc.perform(post("/api/chat/send")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reply").exists());
    }

    @Test
    @DisplayName("空 message 返回 400")
    void shouldReturn400ForBlankMessage() throws Exception {
        ChatRequest req = new ChatRequest();
        req.setMessage("");
        req.setContext("product");

        mockMvc.perform(post("/api/chat/send")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("无 context 返回正常处理")
    void shouldHandleNullContext() throws Exception {
        ChatRequest req = new ChatRequest();
        req.setMessage("数据概览");
        req.setContext(null);

        mockMvc.perform(post("/api/chat/send")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("无 Token 返回 401")
    void shouldReturn401WithoutToken() throws Exception {
        ChatRequest req = new ChatRequest();
        req.setMessage("测试");
        req.setContext("product");

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is(401));
    }
}
```

- [ ] **Step 4: 运行全部测试**

Run: `mvn test`
Expected: All tests pass (BUILD SUCCESS)

- [ ] **Step 5: 提交**

```bash
git add src/test/java/com/example/scaffold/module/chat/
git commit -m "test(chat): add unit tests for AnomalyRuleEngine, MockLlmServiceImpl, ChatController

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 7: 添加聊天面板 CSS 样式

**Files:**
- Modify: `src/main/resources/static/css/app.css`

- [ ] **Step 1: 在 app.css 末尾追加聊天面板样式**

在 `app.css` 文件末尾追加以下内容：

```css
/* ============================================================
   AI Chat — 聊天面板样式
   ============================================================ */

/* ── 悬浮按钮 ──────────────────────────────────────────── */
.ai-fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--accent), var(--accent-light));
  color: var(--bg-deep);
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-body);
  cursor: pointer;
  z-index: 998;
  box-shadow: 0 4px 20px var(--accent-glow);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}
.ai-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 28px var(--accent-glow);
}
.ai-fab:active {
  transform: scale(0.95);
}

/* ── 遮罩 ───────────────────────────────────────────────── */
.chat-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 999;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.25s ease, visibility 0.25s ease;
}
.chat-overlay.active {
  opacity: 1;
  visibility: visible;
}

/* ── 抽屉面板 ───────────────────────────────────────────── */
.chat-drawer {
  position: fixed;
  top: 0;
  right: 0;
  width: 400px;
  height: 100vh;
  background: var(--bg-primary);
  border-left: 1px solid var(--border);
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.chat-drawer.active {
  transform: translateX(0);
}

/* 头部 */
.chat-header {
  background: var(--bg-deep);
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.chat-title {
  color: var(--accent-light);
  font-weight: 700;
  font-size: 15px;
  font-family: var(--font-body);
}
.chat-context {
  color: var(--text-muted);
  font-size: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 2px 10px;
}
.chat-close {
  margin-left: auto;
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 18px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: color var(--transition);
}
.chat-close:hover {
  color: var(--text-primary);
}

/* 消息区 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 用户气泡 */
.msg-user {
  align-self: flex-end;
  background: var(--accent);
  color: var(--bg-deep);
  padding: 8px 14px;
  border-radius: 14px 14px 2px 14px;
  max-width: 85%;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

/* AI 气泡 */
.msg-ai {
  align-self: flex-start;
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-primary);
  padding: 10px 14px;
  border-radius: 14px 14px 14px 2px;
  max-width: 90%;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

/* ── 异常卡片 ───────────────────────────────────────────── */
.anomaly-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-left: 3px solid var(--danger);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  margin-top: 6px;
  transition: border-color var(--transition);
}
.anomaly-card.severity-warning {
  border-left-color: var(--warning);
}
.anomaly-card.severity-info {
  border-left-color: var(--teal);
}
.anomaly-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.anomaly-card-header .severity-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.severity-badge.severity-严重 {
  background: rgba(239, 68, 68, 0.15);
  color: var(--danger);
}
.severity-badge.severity-警告 {
  background: rgba(245, 158, 11, 0.15);
  color: var(--warning);
}
.severity-badge.severity-提示 {
  background: rgba(45, 212, 191, 0.15);
  color: var(--teal);
}
.anomaly-card-header .anomaly-type {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
}
.anomaly-card-body {
  color: var(--text-secondary);
  font-size: 11px;
  line-height: 1.6;
  margin-bottom: 6px;
}
.anomaly-card-actions {
  display: flex;
  gap: 6px;
}
.anomaly-card-actions button {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition);
}
.anomaly-card-actions .btn-detail {
  background: var(--accent);
  color: var(--bg-deep);
}
.anomaly-card-actions .btn-detail:hover {
  background: var(--accent-light);
}
.anomaly-card-actions .btn-dismiss {
  background: var(--bg-elevated);
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
}
.anomaly-card-actions .btn-dismiss:hover {
  background: var(--bg-hover);
}

/* 图片占位 */
.chart-placeholder {
  background: var(--bg-card);
  border: 1px dashed var(--border-light);
  border-radius: var(--radius-sm);
  padding: 20px;
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 8px;
}

/* ── 快捷问题 ───────────────────────────────────────────── */
.quick-chips {
  padding: 8px 12px;
  border-top: 1px solid var(--bg-hover);
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.quick-chips .chip {
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 11px;
  cursor: pointer;
  transition: all var(--transition);
  user-select: none;
  font-family: var(--font-body);
}
.quick-chips .chip:hover {
  background: var(--bg-hover);
  border-color: var(--accent);
  color: var(--accent-light);
}

/* ── 输入区域 ───────────────────────────────────────────── */
.chat-input-area {
  padding: 10px 12px;
  border-top: 1px solid var(--border);
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.chat-input-area input {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 8px 12px;
  color: var(--text-primary);
  font-size: 13px;
  font-family: var(--font-body);
  transition: all var(--transition);
}
.chat-input-area input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-glow);
}
.chat-input-area input::placeholder {
  color: var(--text-muted);
}
.chat-input-area button {
  background: var(--accent);
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  color: var(--bg-deep);
  font-weight: 600;
  font-size: 13px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 2px 8px var(--accent-glow);
}
.chat-input-area button:hover {
  background: var(--accent-light);
  box-shadow: 0 4px 16px var(--accent-glow);
}

/* ── 详情弹窗 ───────────────────────────────────────────── */
.anomaly-detail {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 1001;
  max-width: 500px;
  width: 90%;
  display: none;
}
.anomaly-detail.active {
  display: block;
  animation: scaleIn 0.2s ease;
}
.anomaly-detail .detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-elevated);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}
.anomaly-detail .detail-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-family: var(--font-body);
}
.anomaly-detail .detail-header button {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 18px;
  cursor: pointer;
  padding: 2px 6px;
}
.anomaly-detail .detail-header button:hover {
  color: var(--text-primary);
}
.anomaly-detail .detail-body {
  padding: 16px 18px;
}
.anomaly-detail .detail-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 13px;
  line-height: 1.6;
}
.anomaly-detail .detail-label {
  color: var(--text-muted);
  width: 80px;
  flex-shrink: 0;
}
.anomaly-detail .detail-value {
  color: var(--text-primary);
  word-break: break-word;
}
.detail-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: none;
}
.detail-overlay.active {
  display: block;
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/resources/static/css/app.css
git commit -m "style(chat): add AI chat panel CSS (fab, drawer, bubbles, anomaly cards, detail modal)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 8: 创建 chat.js 交互逻辑

**Files:**
- Create: `src/main/resources/static/js/chat.js`

- [ ] **Step 1: 创建 chat.js**

```javascript
/**
 * AI Chat Manager — 管理聊天面板的完整交互逻辑
 * 依赖：Layui layer（用于 toast），fetch API，localStorage
 */
var ChatManager = {
  context: '',
  isOpen: false,

  // ── 初始化 ────────────────────────────────────────────
  init: function () {
    var self = this;

    document.getElementById('aiFab').addEventListener('click', function () {
      self.toggle();
    });

    document.getElementById('chatClose').addEventListener('click', function () {
      self.close();
    });

    document.getElementById('chatOverlay').addEventListener('click', function () {
      self.close();
    });

    document.getElementById('chatSend').addEventListener('click', function () {
      var input = document.getElementById('chatInput');
      var msg = input.value.trim();
      if (msg) {
        self.send(msg);
        input.value = '';
      }
    });

    document.getElementById('chatInput').addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        document.getElementById('chatSend').click();
      }
    });

    document.querySelectorAll('#quickChips .chip').forEach(function (chip) {
      chip.addEventListener('click', function () {
        self.sendQuick(this.getAttribute('data-prompt'));
      });
    });

    var iframe = document.getElementById('contentFrame');
    if (iframe) {
      iframe.addEventListener('load', function () {
        self.onIframeChange();
      });
      self.onIframeChange();
    }
  },

  // ── 上下文检测 ────────────────────────────────────────
  detectContext: function () {
    var iframe = document.getElementById('contentFrame');
    if (!iframe) return '';

    try {
      var src = iframe.contentWindow.location.pathname;
      if (src.includes('/page/product')) return 'product';
      if (src.includes('/page/user')) return 'user';
      if (src.includes('/page/article')) return 'article';
    } catch (e) {
      // 跨域或未加载
    }
    return '';
  },

  onIframeChange: function () {
    var ctx = this.detectContext();
    if (ctx && ctx !== this.context) {
      this.context = ctx;
      var label = { product: '商品', user: '用户', article: '文章' }[ctx] || ctx;
      document.getElementById('chatContext').textContent = label;
    }
  },

  // ── 面板控制 ──────────────────────────────────────────
  open: function () {
    this.isOpen = true;
    document.getElementById('chatDrawer').classList.add('active');
    document.getElementById('chatOverlay').classList.add('active');
    this.autoSend();
  },

  close: function () {
    this.isOpen = false;
    document.getElementById('chatDrawer').classList.remove('active');
    document.getElementById('chatOverlay').classList.remove('active');
  },

  toggle: function () {
    if (this.isOpen) { this.close(); } else { this.open(); }
  },

  // ── 消息发送 ──────────────────────────────────────────
  send: function (message) {
    var self = this;
    self.renderMessage({ role: 'user', content: message });

    var loadingEl = self.renderLoading();
    var token = localStorage.getItem('token');

    fetch('/api/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify({ message: message, context: self.context })
    })
      .then(function (res) {
        if (res.status === 401) {
          localStorage.removeItem('token');
          localStorage.removeItem('username');
          location.href = '/login';
          throw new Error('Unauthorized');
        }
        return res.json();
      })
      .then(function (res) {
        if (loadingEl) loadingEl.remove();
        if (res.code === 200) {
          self.renderMessage({ role: 'ai', content: res.data });
        } else {
          self.renderMessage({ role: 'ai', content: { reply: '❌ ' + (res.message || '服务异常') } });
        }
      })
      .catch(function (err) {
        if (loadingEl) loadingEl.remove();
        if (err.message !== 'Unauthorized') {
          self.renderMessage({ role: 'ai', content: { reply: '❌ AI 服务暂时不可用，请稍后重试' } });
        }
      });
  },

  autoSend: function () {
    var label = { product: '商品', user: '用户', article: '文章' }[this.context] || '系统';
    this.send('罗列' + label + '的异常情况');
  },

  // ── 渲染 ──────────────────────────────────────────────
  renderMessage: function (msg) {
    var container = document.getElementById('chatMessages');

    if (msg.role === 'user') {
      var el = document.createElement('div');
      el.className = 'msg-user';
      el.textContent = msg.content;
      container.appendChild(el);
    } else {
      var data = msg.content;
      var reply = typeof data === 'string' ? data : (data.reply || '');

      if (reply) {
        var el = document.createElement('div');
        el.className = 'msg-ai';
        el.textContent = reply;
        container.appendChild(el);
      }

      if (data && data.anomalies && data.anomalies.length > 0) {
        data.anomalies.forEach(function (a) {
          container.appendChild(ChatManager.createAnomalyCard(a));
        });
      }

      if (data && data.imagePlaceholder) {
        var placeholder = document.createElement('div');
        placeholder.className = 'chart-placeholder';
        placeholder.textContent = '📊 图表区域（接入真实 AI 后启用）';
        container.appendChild(placeholder);
      }
    }

    container.scrollTop = container.scrollHeight;
  },

  renderLoading: function () {
    var container = document.getElementById('chatMessages');
    var el = document.createElement('div');
    el.className = 'msg-ai';
    el.textContent = '思考中...';
    el.id = 'chatLoading';
    container.appendChild(el);
    container.scrollTop = container.scrollHeight;
    return el;
  },

  createAnomalyCard: function (a) {
    var card = document.createElement('div');
    var sevClass = '';
    if (a.severity === '警告') sevClass = 'severity-warning';
    else if (a.severity === '提示') sevClass = 'severity-info';

    card.className = 'anomaly-card ' + sevClass;
    card.innerHTML =
      '<div class="anomaly-card-header">' +
        '<span class="severity-badge severity-' + a.severity + '">' + a.severity + '</span>' +
        '<span class="anomaly-type">' + ChatManager.escapeHtml(a.type) + '</span>' +
      '</div>' +
      '<div class="anomaly-card-body">' +
        '字段: ' + ChatManager.escapeHtml(a.field) +
        ' | 实际值: ' + ChatManager.escapeHtml(a.actualValue) +
        ' | 期望值: ' + ChatManager.escapeHtml(a.expectedValue) +
      '</div>' +
      '<div class="anomaly-card-actions">' +
        '<button class="btn-detail">详情</button>' +
        '<button class="btn-dismiss">忽略</button>' +
      '</div>';

    card.querySelector('.btn-detail').addEventListener('click', function () {
      ChatManager.showDetail(a);
    });

    card.querySelector('.btn-dismiss').addEventListener('click', function () {
      card.style.opacity = '0.5';
      card.querySelector('.btn-detail').disabled = true;
      card.querySelector('.btn-dismiss').disabled = true;
      card.querySelector('.btn-dismiss').textContent = '已忽略';
    });

    return card;
  },

  // ── 详情弹窗 ──────────────────────────────────────────
  showDetail: function (a) {
    document.getElementById('detailTitle').textContent = a.type + ' — ' + (a.recordName || '');
    document.getElementById('detailBody').innerHTML =
      '<div class="detail-row"><span class="detail-label">异常类型</span><span class="detail-value">' + ChatManager.escapeHtml(a.type) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">严重程度</span><span class="detail-value">' + ChatManager.escapeHtml(a.severity) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">涉及字段</span><span class="detail-value">' + ChatManager.escapeHtml(a.field) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">实际值</span><span class="detail-value">' + ChatManager.escapeHtml(a.actualValue) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">期望值</span><span class="detail-value">' + ChatManager.escapeHtml(a.expectedValue) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">关联记录</span><span class="detail-value">' + ChatManager.escapeHtml(a.recordName || '') + ' (ID: ' + (a.recordId || '-') + ')</span></div>' +
      '<div class="detail-row"><span class="detail-label">修复建议</span><span class="detail-value">' + ChatManager.escapeHtml(a.suggestion || '') + '</span></div>';

    var overlay = document.getElementById('detailOverlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'detailOverlay';
      overlay.className = 'detail-overlay';
      overlay.addEventListener('click', function () { ChatManager.closeDetail(); });
      document.body.appendChild(overlay);
    }

    document.getElementById('anomalyDetail').classList.add('active');
    overlay.classList.add('active');
  },

  closeDetail: function () {
    document.getElementById('anomalyDetail').classList.remove('active');
    var overlay = document.getElementById('detailOverlay');
    if (overlay) overlay.classList.remove('active');
  },

  // ── 快捷问题 ──────────────────────────────────────────
  sendQuick: function (prompt) {
    this.send(prompt);
  },

  // ── 工具 ──────────────────────────────────────────────
  escapeHtml: function (text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

// ── 页面加载完成后初始化 ────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
  if (localStorage.getItem('token')) {
    ChatManager.init();
  }
});
```

- [ ] **Step 2: 提交**

```bash
git add src/main/resources/static/js/chat.js
git commit -m "feat(chat): add ChatManager JS for chat panel interaction

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 9: 修改 index.html — 嵌入聊天面板

**Files:**
- Modify: `src/main/resources/templates/index.html`

- [ ] **Step 1: 在 index.html 的 `</body>` 前追加 HTML 和 JS 引用**

在 `index.html` 中 `</body>` 标签（第 107 行附近）**之前**插入：

```html
<!-- ════════════════════════════════════════════════════════════
     AI Chat — 聊天助手面板
     ════════════════════════════════════════════════════════════ -->

<!-- 悬浮按钮 -->
<button id="aiFab" class="ai-fab" title="AI 助手">
  <span class="ai-fab-icon">AI</span>
</button>

<!-- 遮罩 -->
<div id="chatOverlay" class="chat-overlay"></div>

<!-- 聊天抽屉 -->
<div id="chatDrawer" class="chat-drawer">
  <div class="chat-header">
    <span class="chat-title">🤖 AI 助手</span>
    <span id="chatContext" class="chat-context">系统</span>
    <button id="chatClose" class="chat-close">✕</button>
  </div>
  <div id="chatMessages" class="chat-messages"></div>
  <div id="quickChips" class="quick-chips">
    <span class="chip" data-prompt="数据概览">📊 数据概览</span>
    <span class="chip" data-prompt="异常详情">🔍 异常详情</span>
    <span class="chip" data-prompt="趋势分析">📈 趋势分析</span>
  </div>
  <div class="chat-input-area">
    <input type="text" id="chatInput" placeholder="输入你的问题...">
    <button id="chatSend">发送</button>
  </div>
</div>

<!-- 异常详情弹窗 -->
<div id="anomalyDetail" class="anomaly-detail">
  <div class="detail-header">
    <h3 id="detailTitle"></h3>
    <button onclick="ChatManager.closeDetail()">✕</button>
  </div>
  <div id="detailBody" class="detail-body"></div>
</div>

<script src="/js/chat.js"></script>
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/resources/templates/index.html
git commit -m "feat(chat): integrate AI chat panel into index.html

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 10: 集成验证

- [ ] **Step 1: 启动应用**

Run: `mvn spring-boot:run`

- [ ] **Step 2: 浏览器访问 `http://localhost:8080/login`，登录后进入首页**

- [ ] **Step 3: 验证清单**

| 验证项 | 预期行为 |
|--------|---------|
| 悬浮按钮显示 | 右下角显示金色 AI 按钮 |
| 点击打开面板 | 右侧滑出聊天抽屉 + 半透明遮罩 |
| 上下文显示 | 头部标签显示当前模块名（如"商品"） |
| 自动发送 | 面板打开后自动出现"罗列商品的异常情况" |
| AI 回复 | 返回异常卡片（如有异常数据） |
| 详情弹窗 | 点击【详情】→ 弹窗显示完整异常信息 |
| 快捷问题 | 点击 chip 标签发送对应问题 |
| 手动输入 | 输入框输入文字 → 点击发送 → 收到回复 |
| 关闭面板 | 点击 ✕ / 遮罩 → 面板滑出消失 |
| iframe 切换 | 导航到用户管理 → 上下文自动切换为"用户" |

- [ ] **Step 4: 运行全部测试确认无回归**

Run: `mvn test`
Expected: All tests pass

---

## 实施顺序

```
Task 1 (DTO) → Task 2 (RuleEngine) → Task 3 (LlmService+Mock)
  → Task 4 (ChatService) → Task 5 (ChatController)
  → Task 6 (Tests) → Task 7 (CSS) → Task 8 (chat.js)
  → Task 9 (index.html) → Task 10 (Integration)
```

后端任务（1-6）可独立完成和测试，前端任务（7-9）可在后端就绪后任意顺序进行。

---

*Generated with [Claude Code](https://claude.com/claude-code)*
