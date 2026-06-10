# 开发指南

## 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 21+ |
| Maven | 3.8+ |
| IDE | IntelliJ IDEA / VS Code |

## 项目结构

```
scaffold/
├── pom.xml
├── db/
│   ├── schema.sql               # 表结构（H2 启动初始化）
│   └── data.sql                 # 测试数据
├── src/main/java/.../
│   ├── common/                  # 公共组件
│   └── module/                  # 业务模块 (auth/user/article/product)
└── src/main/resources/
    ├── static/css/app.css       # 全局样式
    └── templates/               # Thymeleaf 页面
```

## 快速开始

```bash
# 克隆并启动（H2 内存数据库，零配置）
git clone <repo-url> scaffold && cd scaffold
mvn spring-boot:run
```

启动后访问：

| 地址 | 说明 |
|------|------|
| http://localhost:8080/ | 应用主页 |
| http://localhost:8080/doc.html | Knife4j API 文档 |
| http://localhost:8080/h2-console | H2 控制台 |
| http://localhost:8080/druid/ | Druid 监控 (admin/admin123) |

## 运行测试

```bash
mvn test                        # 全部测试
mvn test -Dtest=UserServiceTest # 单个测试类
```

## 代码规范

- **控制器**: `@RequiredArgsConstructor` 构造注入
- **实体类**: `@Data` + `@TableName("t_xxx")`
- **API 返回**: `R.ok(data)` / `R.fail("msg")`
- **业务异常**: `throw new BusinessException("msg")`
- **操作日志**: `@Log("描述")` 注解，AOP 自动记录

## 添加新模块

1. 在 `module/<name>/` 下创建 `controller/entity/mapper/service/impl`
2. Entity 加 `@TableName`，Mapper 继承 `BaseMapper<T>`
3. Service 继承 `IService<T>` + `ServiceImpl<M, T>`
4. Controller 使用 `@RequestMapping("/api/<name>")`
5. 如需页面：`templates/<name>/list.html` + `PageController` 添加路由
