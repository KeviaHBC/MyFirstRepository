# AI 聊天助手 — 设计文档

> 日期: 2026-06-10 | 状态: 设计完成 | 分支: main-superpower

## 1. 概述

在现有 Spring Boot + Thymeleaf + Layui 后台管理系统中，新增 AI 聊天助手功能。用户在任意数据管理页面通过右下角悬浮按钮打开 AI 聊天面板，系统自动根据当前页面上下文发送异常检测指令，AI 返回结构化异常列表并支持详情查看。

### 1.1 核心功能

| 功能 | 描述 |
|------|------|
| 悬浮入口 | 页面右下角固定 AI 按钮，点击打开聊天面板 |
| 上下文感知 | 自动检测当前 iframe 页面模块（user/article/product） |
| 自动异常检测 | 打开面板时自动发送"罗列{模块}的异常情况"指令 |
| 异常卡片列表 | AI 返回异常列表，含类型/字段/实际值/期望值/严重程度 |
| 详情弹窗 | 点击异常卡片【详情】按钮弹出完整信息 |
| 快捷问题 | 输入框上方固定 chip 标签（数据概览、异常详情、趋势分析） |
| 图片占位 | 异常列表底部预留图表区域（接入真实 AI 后启用） |
| Mock 引擎 | 第一阶段使用规则引擎模拟 AI，LlmService 接口支持后续替换 |

## 2. 架构

```
前端层 (index.html + app.css + chat.js)
  ├── FloatingButton    — 悬浮按钮
  ├── ChatDrawer        — 抽屉面板（右侧滑入）
  ├── MessageBubble     — 用户/AI 气泡
  ├── AnomalyCard       — 异常卡片
  ├── QuickChips        — 快捷问题标签
  ├── DetailModal       — 详情弹窗
  └── ContextDetector   — iframe URL 上下文检测
        │  REST API (JWT Bearer Token)
        ▼
后端 API 层 (Spring Boot)
  ├── ChatController    — POST /api/chat/send
  ├── ChatService       — 业务编排
  └── GlobalExceptionHandler — 统一异常处理（复用现有）
        │
        ▼
Mock AI 服务层（可替换）
  ├── LlmService (接口)      — ChatResponse chat(ChatRequest)
  ├── MockLlmServiceImpl      — 关键词意图路由
  └── AnomalyRuleEngine       — 规则匹配引擎
        │
        ▼
数据层
  ├── UserService / ArticleService / ProductService（复用现有）
  └── H2 内存数据库
```

## 3. 数据流

### 3.1 自动异常检测流程

1. 用户点击 AI 悬浮按钮
2. `ContextDetector` 检测当前 iframe URL（如 `/page/product`）→ 提取模块名 `"product"`
3. 前端自动发送 `POST /api/chat/send`，Body: `{ "message": "罗列商品的异常情况", "context": "product" }`
4. `ChatController` → `ChatService` → `MockLlmServiceImpl`
5. `MockLlmServiceImpl` 匹配关键词"异常" → 调用 `AnomalyRuleEngine.analyze("product")`
6. `AnomalyRuleEngine` 查询 `ProductService.list()` 获取全量数据，逐条应用规则
7. 返回 `ChatResponse`（含 `anomalies` 列表 + `imagePlaceholder: true`）
8. 前端渲染异常卡片 + 图片占位区

### 3.2 快捷问题流程

1. 用户点击 chip "📊 数据概览"
2. 前端调用 `ChatManager.send("数据概览")`
3. 后端匹配关键词"概览" → 查询对应模块统计数据 → 返回摘要文本

## 4. 前端设计

### 4.1 文件变更

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/main/resources/templates/index.html` | 修改 | 新增悬浮按钮 + 聊天抽屉 + 详情弹窗 HTML，引用 chat.js |
| `src/main/resources/static/css/app.css` | 修改 | 新增 ~200 行 CSS |
| `src/main/resources/static/js/chat.js` | **新增** | ~180 行 ChatManager 交互逻辑 |

### 4.2 HTML 结构

```html
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
    <span id="chatContext" class="chat-context">商品</span>
    <button id="chatClose" class="chat-close">✕</button>
  </div>
  <div id="chatMessages" class="chat-messages"></div>
  <div id="quickChips" class="quick-chips">
    <span class="chip" data-prompt="数据概览">📊 数据概览</span>
    <span class="chip" data-prompt="异常详情">🔍 异常详情</span>
    <span class="chip" data-prompt="趋势分析">📈 趋势分析</span>
  </div>
  <div class="chat-input-area">
    <input id="chatInput" placeholder="输入你的问题...">
    <button id="chatSend">发送</button>
  </div>
</div>

<!-- 异常详情弹窗 -->
<div id="anomalyDetail" class="anomaly-detail" style="display:none;">
  <div class="detail-header">
    <h3 id="detailTitle"></h3>
    <button onclick="ChatManager.closeDetail()">✕</button>
  </div>
  <div id="detailBody" class="detail-body"></div>
</div>
```

### 4.3 CSS 关键类

- `.ai-fab` — fixed bottom:24px right:24px, 52px 圆形, 渐变金色背景, hover 放大 + 光晕
- `.chat-drawer` — fixed right:0 top:0, w:400px, h:100vh, translateX(100%)→0 滑入动画, z-index: 1000
- `.chat-overlay` — fixed inset:0, rgba(0,0,0,0.4), fade 动画, z-index: 999
- `.msg-user` — 右对齐, 金色气泡, border-radius:12px 12px 2px 12px
- `.msg-ai` — 左对齐, 深色卡片, border-radius:12px 12px 12px 2px
- `.anomaly-card` — 卡片容器, 严重程度左边框颜色标识, [详情]/[忽略] 按钮
- `.quick-chips` — flex-wrap, chip 标签 hover 高亮
- `.anomaly-detail` — fixed 居中弹窗, max-w:500px, 表格式字段布局
- 所有新样式复用 Noir Amber CSS 变量体系 (`--bg-*`, `--text-*`, `--accent*`, `--border*`)

### 4.4 ChatManager API

```javascript
var ChatManager = {
  context: '',          // 当前模块 'user' | 'article' | 'product'
  isOpen: false,

  init(),               // 绑定事件，启动 iframe 监听
  detectContext(),      // 从 iframe src 提取模块名
  onIframeChange(),     // iframe load 事件 → 更新 context

  open(),               // 显示抽屉+遮罩 → autoSend()
  close(),              // 隐藏抽屉+遮罩
  toggle(),             // 切换

  send(message),        // POST /api/chat/send → renderMessage()
  autoSend(),           // 构建"罗列{模块}的异常情况" → send()

  renderMessage(msg),   // 渲染用户/AI 气泡
  renderAnomalyCard(a), // 渲染异常卡片
  showDetail(a),        // 弹出详情弹窗
  closeDetail(),        // 关闭详情弹窗
  sendQuick(prompt),    // chip 点击 → send(prompt)
};
```

## 5. 后端设计

### 5.1 文件清单

| 文件 | 包路径 | 说明 |
|------|--------|------|
| `ChatController.java` | `module.chat.controller` | POST /api/chat/send |
| `ChatService.java` | `module.chat.service` | 接口 |
| `ChatServiceImpl.java` | `module.chat.service.impl` | 业务编排 |
| `LlmService.java` | `module.chat.llm` | LLM 调用接口（核心抽象） |
| `MockLlmServiceImpl.java` | `module.chat.llm` | Mock 实现 |
| `AnomalyRuleEngine.java` | `module.chat.llm` | 规则引擎 |
| `ChatRequest.java` | `module.chat.dto` | 请求 DTO |
| `ChatResponse.java` | `module.chat.dto` | 响应 DTO |
| `AnomalyInfo.java` | `module.chat.dto` | 异常信息 DTO |

### 5.2 DTO 定义

```java
// ChatRequest
public class ChatRequest {
    @NotBlank private String message;  // 用户消息
    private String context;            // 模块上下文 "product"|"user"|"article"
}

// ChatResponse
public class ChatResponse {
    private String reply;                    // AI 文本回复
    private List<AnomalyInfo> anomalies;     // 异常列表（可为空）
    private boolean imagePlaceholder;        // 是否显示图片占位
}

// AnomalyInfo
public class AnomalyInfo {
    private String type;           // 异常类型，如"价格异常"
    private String field;          // 涉及字段，如"price"
    private String actualValue;    // 实际值
    private String expectedValue;  // 期望值
    private String severity;       // "严重" | "警告" | "提示"
    private Long recordId;         // 关联记录 ID
    private String recordName;     // 关联记录名称
    private String suggestion;     // 修复建议
}
```

### 5.3 LlmService 接口

```java
public interface LlmService {
    ChatResponse chat(ChatRequest request);
}
```

Mock 实现通过关键词匹配做意图路由：

| 匹配关键词 | 路由目标 | 说明 |
|-----------|---------|------|
| "异常" | AnomalyRuleEngine.analyze(context) | 规则引擎检测 |
| "概览" | 数据统计摘要 | 返回总数、状态分布 |
| 其他 | 通用 Mock 回复 | "当前为 Mock 模式..." |

### 5.4 检测规则

| 模块 | 规则 | 触发条件 | 严重程度 |
|------|------|---------|---------|
| product | 价格异常 | price <= 0 | 严重 |
| product | 库存不足 | stock < 0 | 警告 |
| user | 缺少邮箱 | email == null | 警告 |
| user | 用户名过长 | username.length > 50 | 提示 |
| article | 标题为空 | title == null | 严重 |

规则引擎设计：
- 使用 `Map<String, List<AnomalyRule>>` 按模块组织规则
- 每条规则包含：名称、断言函数（接收实体返回 boolean）、严重程度、建议文本
- 调用现有 `XxxService.list()` 获取全量数据后逐条匹配
- 可通过 `application.yml` 或代码配置轻松增删规则

## 6. 错误处理

| 场景 | 处理方式 |
|------|---------|
| 未登录/Token 过期 | JwtInterceptor 拦截，返回 401，前端跳转登录页 |
| context 参数为空 | 后端使用默认上下文或返回提示"请先导航到具体模块页面" |
| 模块无数据 | AnomalyRuleEngine 返回空列表，回复"未发现异常" |
| API 调用失败 | 前端 catch 后显示错误气泡"AI 服务暂时不可用" |
| 未知模块 | 后端记录 WARN 日志，返回通用回复 |

复用现有 `GlobalExceptionHandler` 统一处理异常。

## 7. 测试策略

### 7.1 后端测试

| 测试类型 | 覆盖内容 |
|---------|---------|
| ChatController 集成测试 | 正常请求、空 message、未知 context |
| MockLlmServiceImpl 单元测试 | 关键词路由正确性、边界条件 |
| AnomalyRuleEngine 单元测试 | 每条规则匹配/不匹配、空数据、混合数据 |
| ChatResponse JSON 序列化 | anomalies 为空列表、imagePlaceholder 字段 |

### 7.2 前端验证

- 悬浮按钮显示/点击响应
- 抽屉面板滑入/滑出动画
- iframe 导航时上下文自动切换
- 异常卡片渲染（含不同严重程度颜色）
- 详情弹窗内容完整性
- 快捷问题 chip 点击发送
- 遮罩点击关闭面板

## 8. 后续扩展

1. **接入真实 AI**: 实现 `OpenAiLlmServiceImpl` / `ClaudeLlmServiceImpl`，替换 `MockLlmServiceImpl`
2. **流式响应**: ChatDrawer 支持 SSE/WebSocket 打字机效果
3. **图片生成**: 图片占位区替换为 ECharts 图表（异常分布饼图、趋势折线图）
4. **对话历史**: 前端 localStorage 存储最近 N 条对话
5. **动态快捷问题**: 根据当前模块和异常检测结果动态生成推荐问题

---

*Generated with [Claude Code](https://claude.com/claude-code)*
