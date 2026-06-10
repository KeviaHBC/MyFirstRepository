# Scaffold API 文档

> Spring Boot 3.4 脚手架项目 · 基地址: `http://localhost:8080` · Knife4j 文档: `/doc.html`

## 认证说明

除登录/注册外，所有 API 请求需携带 JWT Token：

```
Authorization: Bearer <token>
```

登录成功后从返回的 `data.token` 获取。

---

## 统一响应格式 `R<T>`

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录 / Token 无效 |
| 500 | 系统异常 |

## 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

---

## 1. 认证模块 `/api/auth`

### 1.1 登录

```
POST /api/auth/login
```

**请求体 (JSON)**

```json
{
  "username": "admin",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**成功响应 (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGci...",
    "username": "admin"
  }
}
```

**错误响应**

| code | message |
|------|---------|
| 500 | 用户名或密码错误 |
| 500 | 账号已被禁用 |

**curl**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

---

### 1.2 注册

```
POST /api/auth/register
```

**请求体 (JSON)**

```json
{
  "username": "newuser",
  "password": "123456",
  "email": "user@example.com",
  "phone": "13800138000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 手机号 |

**成功响应 (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误响应**

| code | message |
|------|---------|
| 500 | 用户名已存在 |

**curl**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"123456","email":"user@example.com"}'
```

---

## 2. 用户管理 `/api/user`

> ⚠️ 需要认证 `Authorization: Bearer <token>`

### 2.1 分页查询

```
GET /api/user/page
```

**查询参数**

| 参数 | 类型 | 默认值 | 必填 | 说明 |
|------|------|--------|------|------|
| page | int | 1 | 否 | 页码 |
| size | int | 10 | 否 | 每页条数 |
| username | string | — | 否 | 用户名（模糊匹配） |

**响应**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "phone": "13800000000",
        "status": 1,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

**curl**

```bash
TOKEN="your-token-here"
curl -X GET "http://localhost:8080/api/user/page?page=1&size=10&username=admin" \
  -H "Authorization: Bearer $TOKEN"
```

---

### 2.2 根据 ID 查询

```
GET /api/user/{id}
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| id | path | long | 用户 ID |

**curl**

```bash
curl -X GET http://localhost:8080/api/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

### 2.3 新增用户

```
POST /api/user
```

**请求体 (JSON)** — 所有字段见 [用户实体](#用户实体-user)

```json
{
  "username": "zhangsan",
  "password": "123456",
  "email": "zhangsan@example.com",
  "phone": "13800138001",
  "status": 1
}
```

**curl**

```bash
curl -X POST http://localhost:8080/api/user \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456","status":1}'
```

---

### 2.4 更新用户

```
PUT /api/user
```

**请求体 (JSON)** — 必须包含 `id`

```json
{
  "id": 1,
  "username": "admin_new",
  "email": "new@example.com",
  "status": 0
}
```

**curl**

```bash
curl -X PUT http://localhost:8080/api/user \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"username":"admin_new","email":"new@example.com"}'
```

---

### 2.5 删除用户 (逻辑删除)

```
DELETE /api/user/{id}
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| id | path | long | 用户 ID |

**curl**

```bash
curl -X DELETE http://localhost:8080/api/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 3. 文章管理 `/api/article`

> ⚠️ 需要认证 `Authorization: Bearer <token>`

接口模式与用户管理一致：

| 方法 | 路径 | 说明 | 查询参数 |
|------|------|------|---------|
| GET | `/api/article/page` | 分页查询 | `page`, `size`, `title` |
| GET | `/api/article/{id}` | 根据 ID 查询 | — |
| POST | `/api/article` | 新增 | — |
| PUT | `/api/article` | 更新 | — |
| DELETE | `/api/article/{id}` | 删除 | — |

### 3.1 分页查询

```bash
curl -X GET "http://localhost:8080/api/article/page?page=1&size=10&title=Spring" \
  -H "Authorization: Bearer $TOKEN"
```

### 3.2 新增文章

```bash
curl -X POST http://localhost:8080/api/article \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot 入门",
    "content": "Spring Boot 是一个快速开发框架...",
    "category": "后端",
    "authorId": 1,
    "status": 1
  }'
```

---

## 4. 商品管理 `/api/product`

> ⚠️ 需要认证 `Authorization: Bearer <token>`

| 方法 | 路径 | 说明 | 查询参数 |
|------|------|------|---------|
| GET | `/api/product/page` | 分页查询 | `page`, `size`, `name` |
| GET | `/api/product/{id}` | 根据 ID 查询 | — |
| POST | `/api/product` | 新增 | — |
| PUT | `/api/product` | 更新 | — |
| DELETE | `/api/product/{id}` | 删除 | — |

### 4.1 分页查询

```bash
curl -X GET "http://localhost:8080/api/product/page?page=1&size=10&name=MacBook" \
  -H "Authorization: Bearer $TOKEN"
```

### 4.2 新增商品

```bash
curl -X POST http://localhost:8080/api/product \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro",
    "description": "Apple M4 Pro 芯片",
    "price": 14999.00,
    "stock": 100,
    "status": 1
  }'
```

---

## 实体 Schema

### 用户实体 `User`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键，自增 |
| username | string | 用户名 |
| password | string | 密码（BCrypt 加密） |
| email | string | 邮箱 |
| phone | string | 手机号 |
| status | int | 状态: 1=正常, 0=禁用 |
| createTime | datetime | 创建时间（自动填充） |
| updateTime | datetime | 更新时间（自动填充） |

### 文章实体 `Article`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键，自增 |
| title | string | 标题 |
| content | string | 内容 |
| category | string | 分类 |
| status | int | 状态: 1=已发布, 0=草稿 |
| authorId | long | 作者 ID |
| createTime | datetime | 创建时间（自动填充） |
| updateTime | datetime | 更新时间（自动填充） |

### 商品实体 `Product`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键，自增 |
| name | string | 商品名称 |
| description | string | 商品描述 |
| price | decimal | 价格 |
| stock | int | 库存数量 |
| status | int | 状态: 1=上架, 0=下架 |
| createTime | datetime | 创建时间（自动填充） |
| updateTime | datetime | 更新时间（自动填充） |

---

## 错误码参考

| HTTP 状态码 | code | 说明 |
|-------------|------|------|
| 200 | 200 | 请求成功 |
| 400 | 400 | 参数校验失败 (MethodArgumentNotValidException) |
| 401 | 401 | 未提供 Token 或格式错误 |
| 401 | 401 | Token 无效或已过期 |
| 200 | 500 | 业务异常 (BusinessException) |
| 200 | 500 | 系统异常 (兜底) |
| 404 | — | 页面不存在 (HTML 响应) |
