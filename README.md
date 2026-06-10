# Scaffold

Spring Boot 3.4 脚手架项目，集成 MyBatis-Plus、Thymeleaf + Layui、JWT 认证、Druid、Knife4j，提供用户/文章/商品的完整 CRUD 示例。

## 快速开始

```bash
git clone <repo-url> scaffold && cd scaffold
mvn spring-boot:run
```

浏览器打开 **http://localhost:8080/**，默认账号 `admin` / `123456`。

## 技术栈

| 组件 | 版本/说明 |
|------|----------|
| Java | 21 |
| Spring Boot | 3.4.5 |
| MyBatis-Plus | 3.5.9 (分页、逻辑删除) |
| Thymeleaf + Layui | 后端渲染 + 后台 UI |
| JWT (Hutool) | 无状态认证 |
| Druid | 连接池 + SQL 监控 |
| Knife4j | Swagger 增强文档 |
| H2 | 开发环境内存数据库 (MySQL 兼容) |

## 模块概览

```
module/
├── auth/      # 登录/注册 + JWT
├── user/      # 用户 CRUD
├── article/   # 文章 CRUD
└── product/   # 商品 CRUD
```

## 文档导航

| 文档 | 说明 |
|------|------|
| [架构文档](docs/architecture.md) | 系统架构、分层设计、认证流程、数据库 |
| [API 文档](docs/api.md) | 全部接口、请求/响应示例、curl 命令 |
| [开发指南](docs/development.md) | 环境搭建、代码规范、如何添加新模块 |

## 许可证

MIT
