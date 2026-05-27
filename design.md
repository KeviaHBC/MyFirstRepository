# Spring Boot 脚手架项目设计方案

> 日期：2026-05-27

---

## 技术栈

| 层面 | 选型 |
|------|------|
| JDK | Java 21 (LTS) |
| 构建 | Maven |
| 框架 | Spring Boot 3.4.x |
| ORM | MyBatis Plus 3.5.x + Code Generator |
| 模板引擎 | Thymeleaf + Layui |
| 数据库 | H2 (开发) / MySQL (可切换) |
| 连接池 | Alibaba Druid |
| 工具 | Lombok + Hutool + Validation |
| 文档 | Knife4j |
| 认证 | JWT 无状态登录 |
| 异常 | 全局异常处理 + 自定义业务异常 |
| 日志 | SLF4J + Logback + AOP 日志切面 |

---

## 项目标识

| 项目 | 值 |
|------|-----|
| Group ID | `com.example` |
| Artifact ID | `scaffold` |
| 项目名称 | `spring-boot-scaffold` |
| 基础包名 | `com.example.scaffold` |

---

## 目录结构

```
scaffold/
├── pom.xml
├── src/main/java/com/example/scaffold/
│   ├── ScaffoldApplication.java              # 启动类
│   ├── common/
│   │   ├── config/
│   │   │   ├── MyBatisPlusConfig.java         # MyBatis Plus 分页插件
│   │   │   ├── Knife4jConfig.java            # Knife4j API 文档配置
│   │   │   ├── WebMvcConfig.java             # 拦截器注册 + 静态资源
│   │   │   └── DruidConfig.java              # Druid 连接池 + 监控
│   │   ├── handler/
│   │   │   └── GlobalExceptionHandler.java   # 全局异常处理
│   │   ├── interceptor/
│   │   │   └── JwtInterceptor.java           # JWT 拦截器
│   │   ├── aspect/
│   │   │   └── LogAspect.java                # AOP 日志切面
│   │   ├── annotation/
│   │   │   └── Log.java                      # 自定义日志注解
│   │   ├── exception/
│   │   │   └── BusinessException.java        # 自定义业务异常
│   │   ├── result/
│   │   │   └── R.java                        # 统一响应体
│   │   └── util/
│   │       └── JwtUtil.java                  # JWT 工具类
│   ├── module/
│   │   ├── auth/
│   │   │   └── controller/
│   │   │       └── AuthController.java        # 登录/注册
│   │   ├── user/
│   │   │   ├── controller/
│   │   │   │   └── UserController.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   └── impl/
│   │   │   │       └── UserServiceImpl.java
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java
│   │   │   └── entity/
│   │   │       └── User.java
│   │   ├── article/
│   │   │   └── ...                            # 同上结构
│   │   └── product/
│   │       └── ...                            # 同上结构
│   └── generator/
│       └── CodeGenerator.java                 # MyBatis Plus 代码生成器
├── src/main/resources/
│   ├── application.yml                        # 主配置 (H2)
│   ├── application-mysql.yml                  # MySQL 切换配置
│   ├── logback-spring.xml                     # 日志配置
│   ├── db/
│   │   ├── schema.sql                         # H2 建表 DDL
│   │   └── data.sql                           # H2 初始数据
│   └── templates/
│       ├── login.html                          # 登录页
│       ├── index.html                          # 首页
│       ├── user/
│       │   └── list.html                       # 用户列表 (Layui 表格)
│       ├── article/
│       │   └── list.html                       # 文章列表
│       └── product/
│           └── list.html                       # 商品列表
└── src/test/java/com/example/scaffold/
    └── ScaffoldApplicationTests.java          # 基础测试
```

---

## 核心实体设计

### User
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| username | String | 用户名，唯一 |
| password | String | 密码 (BCrypt 加密存储) |
| email | String | 邮箱 |
| phone | String | 手机号 |
| status | Integer | 状态 (1-正常, 0-禁用) |
| create_time | LocalDateTime | 创建时间 |
| update_time | LocalDateTime | 更新时间 |

### Article
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| title | String | 标题 |
| content | String | 内容 (TEXT) |
| category | String | 分类 |
| status | Integer | 状态 (1-发布, 0-草稿) |
| author_id | Long | 作者 ID |
| create_time | LocalDateTime | 创建时间 |
| update_time | LocalDateTime | 更新时间 |

### Product
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| name | String | 商品名称 |
| description | String | 商品描述 |
| price | BigDecimal | 价格 |
| stock | Integer | 库存 |
| status | Integer | 状态 (1-上架, 0-下架) |
| create_time | LocalDateTime | 创建时间 |
| update_time | LocalDateTime | 更新时间 |

---

## JWT 认证

### 流程

```
用户登录 → POST /api/auth/login (username + password)
       → 验证成功，返回 JWT Token
       → 前端存储 Token (localStorage)
       → 后续请求 Header: Authorization: Bearer {token}
       → JwtInterceptor 校验 Token 有效性
```

### Token 设计

- 签发主体：`userId`
- 过期时间：可配置，默认 7 天
- 载荷：`userId`、`username`
- 签名算法：HS256
- 工具：Hutool JWT

### 白名单路径

- `/api/auth/login`、`/api/auth/register`
- `/druid/**`
- `/doc.html`、`/v3/api-docs/**`、`/swagger-resources/**`、`/webjars/**` (Knife4j)
- `/css/**`、`/js/**`、`/images/**` (静态资源)

---

## 全局异常处理

| 异常类型 | HTTP 状态码 | 说明 |
|------|------|------|
| `MethodArgumentNotValidException` | 400 | 参数校验失败 |
| `BindException` | 400 | 参数绑定失败 |
| `BusinessException` | 自定义 code | 业务异常，携带错误码和消息 |
| `Exception` | 500 | 未知异常，记录日志 |

统一返回格式：`R<T>`，包含 `code`、`message`、`data`。

---

## 日志记录

### AOP 日志切面

- 自定义 `@Log` 注解，标注在 Controller 方法上
- 自动记录：请求 URL、请求参数、响应结果、耗时 (ms)
- 异常时记录完整堆栈

### Logback 配置

- 控制台输出：开发环境，DEBUG 级别
- 文件输出：按天滚动，保留 30 天
- 日志路径：`logs/`
- Druid SQL 慢查询日志：阈值 1000ms

---

## Druid 配置

- 替换 HikariCP 为 Druid 连接池
- 开启 StatViewServlet：`/druid/*`，配置登录用户名密码
- 开启 WallFilter (SQL 防火墙) + StatFilter (SQL 监控)
- H2 和 MySQL 两套 Druid 配置，通过 `application.yml` 和 `application-mysql.yml` 分离

---

## CRUD 示例功能

每个模块 (User / Article / Product) 提供：

- **REST API**：分页列表、新增、编辑、删除、详情
- **Thymeleaf 页面**：Layui 表格 + 搜索 + 新增/编辑弹窗
- **Knife4j 文档**：自动生成接口文档，可在线调试
