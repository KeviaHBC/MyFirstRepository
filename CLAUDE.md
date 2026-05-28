# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Spring Boot 3.4.5 脚手架项目，Java 21，Maven 构建。集成 MyBatis-Plus 3.5.9、Thymeleaf + Layui 前端、JWT 认证、Druid 连接池、Knife4j API 文档。

## 常用命令

```bash
# 启动应用（开发环境使用 H2 内存数据库）
mvn spring-boot:run

# 运行全部测试
mvn test

# 运行单个测试类
mvn test -Dtest=ScaffoldApplicationTests

# 打包
mvn clean package -DskipTests

# 代码生成器（连接 MySQL 后运行 main 方法）
# 入口: src/main/java/com/example/scaffold/generator/CodeGenerator.java
```

## 架构

### 模块分层

```
module/
  {moduleName}/
    controller/   # REST API 控制器
    entity/       # MyBatis-Plus 实体类 (对应数据库表)
    mapper/       # MyBatis-Plus BaseMapper 接口
    service/      # 服务接口 (继承 IService)
      impl/       # 服务实现 (继承 ServiceImpl)
```

当前模块: `user`, `article`, `product`, `auth`。

### 认证与拦截

- `JwtInterceptor` 拦截所有 `/api/**` 请求（`/api/auth/login` 和 `/api/auth/register` 除外）
- JWT 使用 Hutool JWT 工具类，密钥和过期时间在 `application.yml` 的 `jwt.secret` / `jwt.expiration` 配置
- 验证通过后在 request 中设置 `userId` 和 `username` 属性
- `/api/**` 之外的页面路由（`/login`, `/index`, `/page/**`）不受拦截，由 `PageController` 处理后端渲染

### 统一响应格式

所有 API 返回 `R<T>` 对象（`common/result/R.java`）：
- `R.ok(data)` — 成功，code=200
- `R.fail("message")` — 失败，code=500
- `R.fail(code, "message")` — 自定义错误码

### 异常处理

`GlobalExceptionHandler` 统一处理三类异常：
- `BusinessException` → 业务异常，返回自定义 code 和 message
- `MethodArgumentNotValidException` → 参数校验失败，返回 400
- `Exception` → 兜底，日志中打印堆栈，返回"系统异常"

### 自定义注解

- `@Log("描述")` — AOP 切面 (`LogAspect`) 自动记录方法调用参数、返回值和耗时

### 数据库

开发环境使用 H2 内存数据库（MySQL 兼容模式）。表结构在 `db/schema.sql`（启动时自动初始化），测试数据在 `db/data.sql`。表名使用 `t_` 前缀（`t_user`, `t_article`, `t_product`）。

MyBatis-Plus 配置了逻辑删除（`deleted` 字段）、下划线转驼峰、ID 自增。

### 依赖要点

- **Hutool** — Java 工具库，项目中使用其 JWT 和 BeanUtil 模块
- **Lombok** — 实体类使用 `@Data`，控制器使用 `@RequiredArgsConstructor` 构造注入
- **Knife4j** — Swagger/OpenAPI 增强文档，可通过 `/doc.html` 访问
- **Druid** — 内置监控页面 `/druid/*`（默认账号 admin/admin123）
- **H2 Console** — `/h2-console` 可查看内存数据库
