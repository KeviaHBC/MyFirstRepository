# Spring Boot 脚手架项目 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot 3.4.x scaffold project with MyBatis Plus, Thymeleaf+Layui, JWT auth, Druid, Knife4j, and CRUD examples for User/Article/Product.

**Architecture:** Standard Spring Boot MVC with annotation-driven config. Common components (R, exception, JWT, AOP log) live in `common/`. Business modules (auth, user, article, product) follow controller → service → mapper → entity layering under `module/`. JWT interceptor protects all `/api/**` routes except auth endpoints.

**Tech Stack:** Java 21, Spring Boot 3.4.x, MyBatis Plus 3.5.x, Druid, H2, Thymeleaf+Layui, Knife4j, Hutool, Lombok

---

### Task 1: 创建项目骨架 (pom.xml + 目录结构 + 启动类)

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/example/scaffold/ScaffoldApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-mysql.yml`
- Create: `src/main/resources/logback-spring.xml`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>scaffold</artifactId>
    <version>1.0.0</version>
    <name>spring-boot-scaffold</name>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
        <hutool.version>5.8.35</hutool.version>
        <knife4j.version>4.5.0</knife4j.version>
        <druid.version>1.2.24</druid.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-generator</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>

        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: scaffold

  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      url: jdbc:h2:mem:scaffold;MODE=MYSQL;DB_CLOSE_DELAY=-1
      username: sa
      password:
      driver-class-name: org.h2.Driver
      initial-size: 5
      min-idle: 5
      max-active: 20
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin123
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000
        wall:
          enabled: true

  h2:
    console:
      enabled: true
      path: /h2-console

  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema.sql
      data-locations: classpath:db/data.sql

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

knife4j:
  enable: true
  setting:
    language: zh_cn

jwt:
  secret: scaffold-jwt-secret-key-2026
  expiration: 604800000
```

- [ ] **Step 3: 创建 application-mysql.yml**

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/scaffold?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: root
      password: root
      driver-class-name: com.mysql.cj.jdbc.Driver

  h2:
    console:
      enabled: false

  sql:
    init:
      mode: never
```

- [ ] **Step 4: 创建 logback-spring.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="logs"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/scaffold.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/scaffold-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>

    <logger name="com.example.scaffold" level="DEBUG"/>
</configuration>
```

- [ ] **Step 5: 创建 ScaffoldApplication.java**

```java
package com.example.scaffold;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.scaffold.module.**.mapper")
public class ScaffoldApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaffoldApplication.class, args);
    }
}
```

- [ ] **Step 6: 验证 Maven 依赖解析**

Run: `mvn dependency:resolve -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add pom.xml src/main/java/com/example/scaffold/ScaffoldApplication.java src/main/resources/
git commit -m "feat: create project skeleton with pom.xml, application configs, and startup class"
```

---

### Task 2: 创建公共组件 (R, BusinessException, JwtUtil, @Log, LogAspect)

**Files:**
- Create: `src/main/java/com/example/scaffold/common/result/R.java`
- Create: `src/main/java/com/example/scaffold/common/exception/BusinessException.java`
- Create: `src/main/java/com/example/scaffold/common/util/JwtUtil.java`
- Create: `src/main/java/com/example/scaffold/common/annotation/Log.java`
- Create: `src/main/java/com/example/scaffold/common/aspect/LogAspect.java`

- [ ] **Step 1: 创建统一响应体 R.java**

```java
package com.example.scaffold.common.result;

import lombok.Data;

@Data
public class R<T> {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.data = data;
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(String message) {
        return fail(500, message);
    }
}
```

- [ ] **Step 2: 创建自定义业务异常 BusinessException.java**

```java
package com.example.scaffold.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(500, message);
    }
}
```

- [ ] **Step 3: 创建 JwtUtil.java**

```java
package com.example.scaffold.common.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(Long userId, String username) {
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("username", username)
                .setExpiresAt(System.currentTimeMillis() + expiration)
                .setKey(secret.getBytes())
                .sign();
    }

    public boolean verify(String token) {
        try {
            return JWTUtil.verify(token, secret.getBytes());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return Long.valueOf(jwt.getPayload("userId").toString());
    }

    public String getUsername(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return jwt.getPayload("username").toString();
    }
}
```

- [ ] **Step 4: 创建 @Log 注解**

```java
package com.example.scaffold.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String value() default "";
}
```

- [ ] **Step 5: 创建 LogAspect.java**

```java
package com.example.scaffold.common.aspect;

import com.example.scaffold.common.annotation.Log;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        String desc = logAnnotation.value().isEmpty() ? methodName : logAnnotation.value();

        log.info("[{}] 请求参数: {}", desc, Arrays.toString(joinPoint.getArgs()));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] 响应结果: {}, 耗时: {}ms", desc, result, elapsed);
            return result;
        } catch (Throwable e) {
            log.error("[{}] 异常: {}", desc, e.getMessage(), e);
            throw e;
        }
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/example/scaffold/common/
git commit -m "feat: add common components - R, BusinessException, JwtUtil, @Log, LogAspect"
```

---

### Task 3: 创建配置类 + 拦截器 + 全局异常处理

**Files:**
- Create: `src/main/java/com/example/scaffold/common/config/MyBatisPlusConfig.java`
- Create: `src/main/java/com/example/scaffold/common/config/Knife4jConfig.java`
- Create: `src/main/java/com/example/scaffold/common/config/WebMvcConfig.java`
- Create: `src/main/java/com/example/scaffold/common/interceptor/JwtInterceptor.java`
- Create: `src/main/java/com/example/scaffold/common/handler/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 MyBatisPlusConfig.java**

```java
package com.example.scaffold.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }
}
```

- [ ] **Step 2: 创建 Knife4jConfig.java**

```java
package com.example.scaffold.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scaffold API")
                        .version("1.0.0")
                        .description("Spring Boot 脚手架项目接口文档"));
    }
}
```

- [ ] **Step 3: 创建 JwtInterceptor.java**

```java
package com.example.scaffold.common.interceptor;

import com.example.scaffold.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        token = token.substring(7);
        if (!jwtUtil.verify(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("username", jwtUtil.getUsername(token));
        return true;
    }
}
```

- [ ] **Step 4: 创建 WebMvcConfig.java**

```java
package com.example.scaffold.common.config;

import com.example.scaffold.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

- [ ] **Step 5: 创建 GlobalExceptionHandler.java**

```java
package com.example.scaffold.common.handler;

import com.example.scaffold.common.exception.BusinessException;
import com.example.scaffold.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return R.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail("系统异常，请联系管理员");
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/example/scaffold/common/config/ src/main/java/com/example/scaffold/common/interceptor/ src/main/java/com/example/scaffold/common/handler/
git commit -m "feat: add config classes, JWT interceptor, and global exception handler"
```

---

### Task 4: 创建数据库脚本 + 实体类 + Mapper

**Files:**
- Create: `src/main/resources/db/schema.sql`
- Create: `src/main/resources/db/data.sql`
- Create: `src/main/java/com/example/scaffold/module/user/entity/User.java`
- Create: `src/main/java/com/example/scaffold/module/user/mapper/UserMapper.java`
- Create: `src/main/java/com/example/scaffold/module/article/entity/Article.java`
- Create: `src/main/java/com/example/scaffold/module/article/mapper/ArticleMapper.java`
- Create: `src/main/java/com/example/scaffold/module/product/entity/Product.java`
- Create: `src/main/java/com/example/scaffold/module/product/mapper/ProductMapper.java`

- [ ] **Step 1: 创建 schema.sql**

```sql
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS t_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content CLOB,
    category VARCHAR(50),
    status INT DEFAULT 0,
    author_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 2: 创建 data.sql**

```sql
INSERT INTO t_user (username, password, email, phone, status) VALUES
('admin', '$2a$10$AmZ/1KpudYuXcVhypUlA6u9I9vM29xjDJmz3cZK9f9hI19e.6ENZ2', 'admin@example.com', '13800000001', 1),
('user1', '$2a$10$AmZ/1KpudYuXcVhypUlA6u9I9vM29xjDJmz3cZK9f9hI19e.6ENZ2', 'user1@example.com', '13800000002', 1),
('user2', '$2a$10$AmZ/1KpudYuXcVhypUlA6u9I9vM29xjDJmz3cZK9f9hI19e.6ENZ2', 'user2@example.com', '13800000003', 0);

INSERT INTO t_article (title, content, category, status, author_id) VALUES
('Spring Boot 入门', 'Spring Boot 是由 Pivotal 团队提供的全新框架...', '技术', 1, 1),
('Java 21 新特性', 'Java 21 引入了虚拟线程、模式匹配等新特性...', '技术', 1, 1),
('设计模式实践', '设计模式是软件开发中的最佳实践...', '编程', 0, 2);

INSERT INTO t_product (name, description, price, stock, status) VALUES
('机械键盘', 'Cherry MX 青轴，87键', 399.00, 100, 1),
('无线鼠标', '蓝牙5.0，静音按键', 129.00, 200, 1),
('显示器支架', '双屏支架，承重8kg', 259.00, 50, 1);
```

- [ ] **Step 3: 创建 User.java**

```java
package com.example.scaffold.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: 创建 UserMapper.java**

```java
package com.example.scaffold.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scaffold.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 5: 创建 Article.java**

```java
package com.example.scaffold.module.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String category;

    private Integer status;

    private Long authorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 6: 创建 ArticleMapper.java**

```java
package com.example.scaffold.module.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scaffold.module.article.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
```

- [ ] **Step 7: 创建 Product.java**

```java
package com.example.scaffold.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 8: 创建 ProductMapper.java**

```java
package com.example.scaffold.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scaffold.module.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
```

- [ ] **Step 9: 提交**

```bash
git add src/main/resources/db/ src/main/java/com/example/scaffold/module/user/entity/ src/main/java/com/example/scaffold/module/user/mapper/ src/main/java/com/example/scaffold/module/article/ src/main/java/com/example/scaffold/module/product/
git commit -m "feat: add database scripts, entity classes, and mapper interfaces"
```

---

### Task 5: 创建 Service 层 (User + Article + Product)

**Files:**
- Create: `src/main/java/com/example/scaffold/module/user/service/UserService.java`
- Create: `src/main/java/com/example/scaffold/module/user/service/impl/UserServiceImpl.java`
- Create: `src/main/java/com/example/scaffold/module/article/service/ArticleService.java`
- Create: `src/main/java/com/example/scaffold/module/article/service/impl/ArticleServiceImpl.java`
- Create: `src/main/java/com/example/scaffold/module/product/service/ProductService.java`
- Create: `src/main/java/com/example/scaffold/module/product/service/impl/ProductServiceImpl.java`

- [ ] **Step 1: 创建 UserService.java**

```java
package com.example.scaffold.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scaffold.module.user.entity.User;

public interface UserService extends IService<User> {
}
```

- [ ] **Step 2: 创建 UserServiceImpl.java**

```java
package com.example.scaffold.module.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.mapper.UserMapper;
import com.example.scaffold.module.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
```

- [ ] **Step 3: 创建 ArticleService.java**

```java
package com.example.scaffold.module.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scaffold.module.article.entity.Article;

public interface ArticleService extends IService<Article> {
}
```

- [ ] **Step 4: 创建 ArticleServiceImpl.java**

```java
package com.example.scaffold.module.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.mapper.ArticleMapper;
import com.example.scaffold.module.article.service.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
}
```

- [ ] **Step 5: 创建 ProductService.java**

```java
package com.example.scaffold.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scaffold.module.product.entity.Product;

public interface ProductService extends IService<Product> {
}
```

- [ ] **Step 6: 创建 ProductServiceImpl.java**

```java
package com.example.scaffold.module.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.mapper.ProductMapper;
import com.example.scaffold.module.product.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}
```

- [ ] **Step 7: 提交**

```bash
git add src/main/java/com/example/scaffold/module/user/service/ src/main/java/com/example/scaffold/module/article/service/ src/main/java/com/example/scaffold/module/product/service/
git commit -m "feat: add service layer for User, Article, and Product"
```

---

### Task 6: 创建 AuthController (登录/注册)

**Files:**
- Create: `src/main/java/com/example/scaffold/module/auth/controller/AuthController.java`
- Create: `src/main/java/com/example/scaffold/common/config/PasswordConfig.java`

- [ ] **Step 1: 创建 PasswordConfig.java (BCrypt Bean)**

```java
package com.example.scaffold.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: 创建 AuthController.java**

```java
package com.example.scaffold.module.auth.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.exception.BusinessException;
import com.example.scaffold.common.result.R;
import com.example.scaffold.common.util.JwtUtil;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Log("用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = Map.of("token", token, "username", user.getUsername());
        return R.ok(result);
    }

    @Log("用户注册")
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        long count = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = BeanUtil.copyProperties(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        userService.save(user);
        return R.ok();
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String email;
        private String phone;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/scaffold/module/auth/ src/main/java/com/example/scaffold/common/config/PasswordConfig.java
git commit -m "feat: add auth controller with login and register endpoints"
```

---

### Task 7: 创建业务 Controllers (User + Article + Product REST API)

**Files:**
- Create: `src/main/java/com/example/scaffold/module/user/controller/UserController.java`
- Create: `src/main/java/com/example/scaffold/module/article/controller/ArticleController.java`
- Create: `src/main/java/com/example/scaffold/module/product/controller/ProductController.java`

- [ ] **Step 1: 创建 UserController.java**

```java
package com.example.scaffold.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    public R<Page<User>> page(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return R.ok(userService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Log("新增用户")
    @PostMapping
    public R<Void> save(@Valid @RequestBody User user) {
        userService.save(user);
        return R.ok();
    }

    @Log("更新用户")
    @PutMapping
    public R<Void> update(@Valid @RequestBody User user) {
        userService.updateById(user);
        return R.ok();
    }

    @Log("删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 创建 ArticleController.java**

```java
package com.example.scaffold.module.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.article.entity.Article;
import com.example.scaffold.module.article.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/page")
    public R<Page<Article>> page(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Article::getTitle, title);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        return R.ok(articleService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Article> getById(@PathVariable Long id) {
        return R.ok(articleService.getById(id));
    }

    @Log("新增文章")
    @PostMapping
    public R<Void> save(@Valid @RequestBody Article article) {
        articleService.save(article);
        return R.ok();
    }

    @Log("更新文章")
    @PutMapping
    public R<Void> update(@Valid @RequestBody Article article) {
        articleService.updateById(article);
        return R.ok();
    }

    @Log("删除文章")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        articleService.removeById(id);
        return R.ok();
    }
}
```

- [ ] **Step 3: 创建 ProductController.java**

```java
package com.example.scaffold.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.product.entity.Product;
import com.example.scaffold.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/page")
    public R<Page<Product>> page(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Product::getName, name);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return R.ok(productService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Product> getById(@PathVariable Long id) {
        return R.ok(productService.getById(id));
    }

    @Log("新增商品")
    @PostMapping
    public R<Void> save(@Valid @RequestBody Product product) {
        productService.save(product);
        return R.ok();
    }

    @Log("更新商品")
    @PutMapping
    public R<Void> update(@Valid @RequestBody Product product) {
        productService.updateById(product);
        return R.ok();
    }

    @Log("删除商品")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/scaffold/module/user/controller/ src/main/java/com/example/scaffold/module/article/controller/ src/main/java/com/example/scaffold/module/product/controller/
git commit -m "feat: add REST API controllers for User, Article, and Product"
```

---

### Task 8: 创建 Thymeleaf 页面 (Layui)

**Files:**
- Create: `src/main/resources/templates/login.html`
- Create: `src/main/resources/templates/index.html`
- Create: `src/main/resources/templates/user/list.html`
- Create: `src/main/resources/templates/article/list.html`
- Create: `src/main/resources/templates/product/list.html`
- Create: `src/main/resources/templates/error/404.html`

- [ ] **Step 1: 创建 login.html**

```html
<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - Scaffold</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/css/layui.css">
</head>
<body style="background: #f0f2f5;">
<div style="display:flex;justify-content:center;align-items:center;min-height:100vh;">
    <div style="width:400px;padding:40px;background:#fff;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,.1);">
        <h2 style="text-align:center;margin-bottom:30px;">Scaffold 脚手架</h2>
        <form class="layui-form" id="loginForm">
            <div class="layui-form-item">
                <input type="text" name="username" lay-verify="required" placeholder="用户名" class="layui-input">
            </div>
            <div class="layui-form-item">
                <input type="password" name="password" lay-verify="required" placeholder="密码" class="layui-input">
            </div>
            <div class="layui-form-item">
                <button lay-submit lay-filter="login" class="layui-btn layui-btn-fluid">登 录</button>
            </div>
        </form>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/layui.js"></script>
<script>
layui.use(['form', 'layer'], function() {
    var form = layui.form, layer = layui.layer;
    form.on('submit(login)', function(data) {
        fetch('/api/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data.field)
        }).then(res => res.json()).then(res => {
            if (res.code === 200) {
                localStorage.setItem('token', res.data.token);
                localStorage.setItem('username', res.data.username);
                location.href = '/index';
            } else {
                layer.msg(res.message);
            }
        });
        return false;
    });
});
</script>
</body>
</html>
```

- [ ] **Step 2: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Scaffold 首页</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/css/layui.css">
</head>
<body>
<div class="layui-layout layui-layout-admin">
    <div class="layui-header">
        <div class="layui-logo">Scaffold 脚手架</div>
        <ul class="layui-nav layui-layout-right">
            <li class="layui-nav-item"><a href="javascript:;" id="usernameText"></a></li>
            <li class="layui-nav-item"><a href="javascript:;" onclick="logout()">退出</a></li>
        </ul>
    </div>
    <div class="layui-side layui-bg-black">
        <ul class="layui-nav layui-nav-tree">
            <li class="layui-nav-item"><a href="/page/user">用户管理</a></li>
            <li class="layui-nav-item"><a href="/page/article">文章管理</a></li>
            <li class="layui-nav-item"><a href="/page/product">商品管理</a></li>
        </ul>
    </div>
    <div class="layui-body" style="padding:15px;">
        <iframe id="contentFrame" style="width:100%;height:100%;border:none;"></iframe>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/layui.js"></script>
<script>
var token = localStorage.getItem('token');
if (!token) { location.href = '/login'; }
document.getElementById('usernameText').textContent = localStorage.getItem('username');

document.querySelectorAll('.layui-nav-tree .layui-nav-item a').forEach(function(a) {
    a.onclick = function() {
        document.getElementById('contentFrame').src = this.getAttribute('href');
        return false;
    };
});

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    location.href = '/login';
}
</script>
</body>
</html>
```

- [ ] **Step 3: 创建 user/list.html**

```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>用户管理</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/css/layui.css">
</head>
<body style="padding:15px;">
<script type="text/html" id="toolbar">
    <button class="layui-btn layui-btn-sm" lay-event="add">新增用户</button>
</script>
<script type="text/html" id="bar">
    <a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
</script>

<div class="layui-form">
    <div class="layui-form-item" style="display:inline-block;width:200px;">
        <input type="text" id="searchUsername" placeholder="搜索用户名" class="layui-input">
    </div>
    <button class="layui-btn" id="searchBtn">搜索</button>
</div>

<table id="userTable" lay-filter="userTable"></table>

<script src="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/layui.js"></script>
<script>
layui.use(['table', 'layer'], function() {
    var table = layui.table, layer = layui.layer;
    table.render({
        elem: '#userTable',
        url: '/api/user/page',
        headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')},
        toolbar: '#toolbar',
        cols: [[
            {field: 'id', title: 'ID', width: 80},
            {field: 'username', title: '用户名'},
            {field: 'email', title: '邮箱'},
            {field: 'phone', title: '手机号'},
            {field: 'status', title: '状态', templet: function(d) { return d.status === 1 ? '正常' : '禁用'; }},
            {fixed: 'right', title: '操作', toolbar: '#bar', width: 150}
        ]],
        page: true
    });

    table.on('toolbar(userTable)', function(obj) {
        if (obj.event === 'add') {
            layer.open({
                type: 1,
                title: '新增用户',
                area: ['500px', '400px'],
                content: '<form class="layui-form" lay-filter="userForm" style="padding:20px;">'
                    + '<div class="layui-form-item"><label class="layui-form-label">用户名</label><div class="layui-input-block"><input type="text" name="username" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">邮箱</label><div class="layui-input-block"><input type="text" name="email" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">手机号</label><div class="layui-input-block"><input type="text" name="phone" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><div class="layui-input-block"><button lay-submit lay-filter="saveUser" class="layui-btn">保存</button></div></div>'
                    + '</form>',
                success: function() {
                    layui.form.render();
                    layui.form.on('submit(saveUser)', function(data) {
                        fetch('/api/user', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token')},
                            body: JSON.stringify(data.field)
                        }).then(res => res.json()).then(res => {
                            if (res.code === 200) { layer.closeAll(); table.reload('userTable'); layer.msg('新增成功'); }
                            else { layer.msg(res.message); }
                        });
                        return false;
                    });
                }
            });
        }
    });

    table.on('tool(userTable)', function(obj) {
        if (obj.event === 'del') {
            layer.confirm('确认删除？', function() {
                fetch('/api/user/' + obj.data.id, {
                    method: 'DELETE',
                    headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')}
                }).then(res => res.json()).then(res => {
                    if (res.code === 200) { table.reload('userTable'); layer.msg('删除成功'); }
                });
            });
        } else if (obj.event === 'edit') {
            var d = obj.data;
            layer.open({
                type: 1,
                title: '编辑用户',
                area: ['500px', '400px'],
                content: '<form class="layui-form" lay-filter="editForm" style="padding:20px;">'
                    + '<input type="hidden" name="id" value="' + d.id + '">'
                    + '<div class="layui-form-item"><label class="layui-form-label">用户名</label><div class="layui-input-block"><input type="text" name="username" value="' + d.username + '" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">邮箱</label><div class="layui-input-block"><input type="text" name="email" value="' + (d.email||'') + '" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">手机号</label><div class="layui-input-block"><input type="text" name="phone" value="' + (d.phone||'') + '" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">状态</label><div class="layui-input-block"><input type="radio" name="status" value="1" title="正常" ' + (d.status===1?'checked':'') + '><input type="radio" name="status" value="0" title="禁用" ' + (d.status===0?'checked':'') + '></div></div>'
                    + '<div class="layui-form-item"><div class="layui-input-block"><button lay-submit lay-filter="updateUser" class="layui-btn">保存</button></div></div>'
                    + '</form>',
                success: function() {
                    layui.form.render();
                    layui.form.on('submit(updateUser)', function(data) {
                        fetch('/api/user', {
                            method: 'PUT',
                            headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token')},
                            body: JSON.stringify(data.field)
                        }).then(res => res.json()).then(res => {
                            if (res.code === 200) { layer.closeAll(); table.reload('userTable'); layer.msg('更新成功'); }
                            else { layer.msg(res.message); }
                        });
                        return false;
                    });
                }
            });
        }
    });

    document.getElementById('searchBtn').onclick = function() {
        table.reload('userTable', {where: {username: document.getElementById('searchUsername').value}});
    };
});
</script>
</body>
</html>
```

- [ ] **Step 4: 创建 article/list.html**

```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>文章管理</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/css/layui.css">
</head>
<body style="padding:15px;">
<script type="text/html" id="toolbar">
    <button class="layui-btn layui-btn-sm" lay-event="add">新增文章</button>
</script>
<script type="text/html" id="bar">
    <a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
</script>

<div class="layui-form">
    <div class="layui-form-item" style="display:inline-block;width:200px;">
        <input type="text" id="searchTitle" placeholder="搜索标题" class="layui-input">
    </div>
    <button class="layui-btn" id="searchBtn">搜索</button>
</div>

<table id="articleTable" lay-filter="articleTable"></table>

<script src="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/layui.js"></script>
<script>
layui.use(['table', 'layer'], function() {
    var table = layui.table, layer = layui.layer;
    table.render({
        elem: '#articleTable',
        url: '/api/article/page',
        headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')},
        toolbar: '#toolbar',
        cols: [[
            {field: 'id', title: 'ID', width: 80},
            {field: 'title', title: '标题'},
            {field: 'category', title: '分类', width: 100},
            {field: 'status', title: '状态', width: 80, templet: function(d) { return d.status === 1 ? '发布' : '草稿'; }},
            {fixed: 'right', title: '操作', toolbar: '#bar', width: 150}
        ]],
        page: true
    });

    table.on('toolbar(articleTable)', function(obj) {
        if (obj.event === 'add') {
            layer.open({
                type: 1,
                title: '新增文章',
                area: ['600px', '500px'],
                content: '<form class="layui-form" lay-filter="articleForm" style="padding:20px;">'
                    + '<div class="layui-form-item"><label class="layui-form-label">标题</label><div class="layui-input-block"><input type="text" name="title" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">分类</label><div class="layui-input-block"><input type="text" name="category" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">内容</label><div class="layui-input-block"><textarea name="content" class="layui-textarea" rows="5"></textarea></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">状态</label><div class="layui-input-block"><input type="radio" name="status" value="1" title="发布" checked><input type="radio" name="status" value="0" title="草稿"></div></div>'
                    + '<div class="layui-form-item"><div class="layui-input-block"><button lay-submit lay-filter="saveArticle" class="layui-btn">保存</button></div></div>'
                    + '</form>',
                success: function() {
                    layui.form.render();
                    layui.form.on('submit(saveArticle)', function(data) {
                        data.field.authorId = 1;
                        fetch('/api/article', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token')},
                            body: JSON.stringify(data.field)
                        }).then(res => res.json()).then(res => {
                            if (res.code === 200) { layer.closeAll(); table.reload('articleTable'); layer.msg('新增成功'); }
                            else { layer.msg(res.message); }
                        });
                        return false;
                    });
                }
            });
        }
    });

    table.on('tool(articleTable)', function(obj) {
        if (obj.event === 'del') {
            layer.confirm('确认删除？', function() {
                fetch('/api/article/' + obj.data.id, {
                    method: 'DELETE',
                    headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')}
                }).then(res => res.json()).then(res => {
                    if (res.code === 200) { table.reload('articleTable'); layer.msg('删除成功'); }
                });
            });
        } else if (obj.event === 'edit') {
            var d = obj.data;
            layer.open({
                type: 1,
                title: '编辑文章',
                area: ['600px', '500px'],
                content: '<form class="layui-form" lay-filter="editArticleForm" style="padding:20px;">'
                    + '<input type="hidden" name="id" value="' + d.id + '">'
                    + '<div class="layui-form-item"><label class="layui-form-label">标题</label><div class="layui-input-block"><input type="text" name="title" value="' + (d.title||'') + '" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">分类</label><div class="layui-input-block"><input type="text" name="category" value="' + (d.category||'') + '" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">内容</label><div class="layui-input-block"><textarea name="content" class="layui-textarea" rows="5">' + (d.content||'') + '</textarea></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">状态</label><div class="layui-input-block"><input type="radio" name="status" value="1" title="发布" ' + (d.status===1?'checked':'') + '><input type="radio" name="status" value="0" title="草稿" ' + (d.status===0?'checked':'') + '></div></div>'
                    + '<div class="layui-form-item"><div class="layui-input-block"><button lay-submit lay-filter="updateArticle" class="layui-btn">保存</button></div></div>'
                    + '</form>',
                success: function() {
                    layui.form.render();
                    layui.form.on('submit(updateArticle)', function(data) {
                        fetch('/api/article', {
                            method: 'PUT',
                            headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token')},
                            body: JSON.stringify(data.field)
                        }).then(res => res.json()).then(res => {
                            if (res.code === 200) { layer.closeAll(); table.reload('articleTable'); layer.msg('更新成功'); }
                            else { layer.msg(res.message); }
                        });
                        return false;
                    });
                }
            });
        }
    });

    document.getElementById('searchBtn').onclick = function() {
        table.reload('articleTable', {where: {title: document.getElementById('searchTitle').value}});
    };
});
</script>
</body>
</html>
```

- [ ] **Step 5: 创建 product/list.html**

```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>商品管理</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/css/layui.css">
</head>
<body style="padding:15px;">
<script type="text/html" id="toolbar">
    <button class="layui-btn layui-btn-sm" lay-event="add">新增商品</button>
</script>
<script type="text/html" id="bar">
    <a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
</script>

<div class="layui-form">
    <div class="layui-form-item" style="display:inline-block;width:200px;">
        <input type="text" id="searchName" placeholder="搜索商品名称" class="layui-input">
    </div>
    <button class="layui-btn" id="searchBtn">搜索</button>
</div>

<table id="productTable" lay-filter="productTable"></table>

<script src="https://cdn.jsdelivr.net/npm/layui@2.9.8/dist/layui.js"></script>
<script>
layui.use(['table', 'layer'], function() {
    var table = layui.table, layer = layui.layer;
    table.render({
        elem: '#productTable',
        url: '/api/product/page',
        headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')},
        toolbar: '#toolbar',
        cols: [[
            {field: 'id', title: 'ID', width: 80},
            {field: 'name', title: '名称'},
            {field: 'price', title: '价格', templet: function(d) { return '¥' + d.price; }},
            {field: 'stock', title: '库存'},
            {field: 'status', title: '状态', templet: function(d) { return d.status === 1 ? '上架' : '下架'; }},
            {fixed: 'right', title: '操作', toolbar: '#bar', width: 150}
        ]],
        page: true
    });

    table.on('toolbar(productTable)', function(obj) {
        if (obj.event === 'add') {
            layer.open({
                type: 1,
                title: '新增商品',
                area: ['500px', '450px'],
                content: '<form class="layui-form" lay-filter="productForm" style="padding:20px;">'
                    + '<div class="layui-form-item"><label class="layui-form-label">名称</label><div class="layui-input-block"><input type="text" name="name" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">描述</label><div class="layui-input-block"><input type="text" name="description" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">价格</label><div class="layui-input-block"><input type="number" name="price" step="0.01" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">库存</label><div class="layui-input-block"><input type="number" name="stock" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">状态</label><div class="layui-input-block"><input type="radio" name="status" value="1" title="上架" checked><input type="radio" name="status" value="0" title="下架"></div></div>'
                    + '<div class="layui-form-item"><div class="layui-input-block"><button lay-submit lay-filter="saveProduct" class="layui-btn">保存</button></div></div>'
                    + '</form>',
                success: function() {
                    layui.form.render();
                    layui.form.on('submit(saveProduct)', function(data) {
                        fetch('/api/product', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token')},
                            body: JSON.stringify(data.field)
                        }).then(res => res.json()).then(res => {
                            if (res.code === 200) { layer.closeAll(); table.reload('productTable'); layer.msg('新增成功'); }
                            else { layer.msg(res.message); }
                        });
                        return false;
                    });
                }
            });
        }
    });

    table.on('tool(productTable)', function(obj) {
        if (obj.event === 'del') {
            layer.confirm('确认删除？', function() {
                fetch('/api/product/' + obj.data.id, {
                    method: 'DELETE',
                    headers: {'Authorization': 'Bearer ' + localStorage.getItem('token')}
                }).then(res => res.json()).then(res => {
                    if (res.code === 200) { table.reload('productTable'); layer.msg('删除成功'); }
                });
            });
        } else if (obj.event === 'edit') {
            var d = obj.data;
            layer.open({
                type: 1,
                title: '编辑商品',
                area: ['500px', '450px'],
                content: '<form class="layui-form" lay-filter="editProductForm" style="padding:20px;">'
                    + '<input type="hidden" name="id" value="' + d.id + '">'
                    + '<div class="layui-form-item"><label class="layui-form-label">名称</label><div class="layui-input-block"><input type="text" name="name" value="' + (d.name||'') + '" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">描述</label><div class="layui-input-block"><input type="text" name="description" value="' + (d.description||'') + '" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">价格</label><div class="layui-input-block"><input type="number" name="price" step="0.01" value="' + (d.price||'') + '" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">库存</label><div class="layui-input-block"><input type="number" name="stock" value="' + (d.stock||'') + '" lay-verify="required" class="layui-input"></div></div>'
                    + '<div class="layui-form-item"><label class="layui-form-label">状态</label><div class="layui-input-block"><input type="radio" name="status" value="1" title="上架" ' + (d.status===1?'checked':'') + '><input type="radio" name="status" value="0" title="下架" ' + (d.status===0?'checked':'') + '></div></div>'
                    + '<div class="layui-form-item"><div class="layui-input-block"><button lay-submit lay-filter="updateProduct" class="layui-btn">保存</button></div></div>'
                    + '</form>',
                success: function() {
                    layui.form.render();
                    layui.form.on('submit(updateProduct)', function(data) {
                        fetch('/api/product', {
                            method: 'PUT',
                            headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token')},
                            body: JSON.stringify(data.field)
                        }).then(res => res.json()).then(res => {
                            if (res.code === 200) { layer.closeAll(); table.reload('productTable'); layer.msg('更新成功'); }
                            else { layer.msg(res.message); }
                        });
                        return false;
                    });
                }
            });
        }
    });

    document.getElementById('searchBtn').onclick = function() {
        table.reload('productTable', {where: {name: document.getElementById('searchName').value}});
    };
});
</script>
</body>
</html>
```

- [ ] **Step 6: 创建 404.html**

```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>404 - 页面不存在</title>
</head>
<body style="text-align:center;padding-top:100px;">
    <h1>404</h1>
    <p>页面不存在</p>
    <a href="/">返回首页</a>
</body>
</html>
```

- [ ] **Step 7: 提交**

```bash
git add src/main/resources/templates/
git commit -m "feat: add Thymeleaf pages with Layui UI for login, index, and CRUD management"
```

---

### Task 9: 创建页面路由 Controller + 代码生成器 + 测试

**Files:**
- Create: `src/main/java/com/example/scaffold/module/PageController.java`
- Create: `src/main/java/com/example/scaffold/generator/CodeGenerator.java`
- Create: `src/test/java/com/example/scaffold/ScaffoldApplicationTests.java`

- [ ] **Step 1: 创建 PageController.java**

```java
package com.example.scaffold.module;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/page/user")
    public String userList() {
        return "user/list";
    }

    @GetMapping("/page/article")
    public String articleList() {
        return "article/list";
    }

    @GetMapping("/page/product")
    public String productList() {
        return "product/list";
    }
}
```

- [ ] **Step 2: 创建 CodeGenerator.java**

```java
package com.example.scaffold.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/scaffold", "root", "root")
                .globalConfig(builder -> builder
                        .author("scaffold")
                        .outputDir("src/main/java")
                        .commentDate("yyyy-MM-dd"))
                .packageConfig(builder -> builder
                        .parent("com.example.scaffold.module")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, "src/main/resources/mapper")))
                .strategyConfig(builder -> builder
                        .addTablePrefix("t_")
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .controllerBuilder()
                        .enableRestStyle())
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
```

- [ ] **Step 3: 创建 ScaffoldApplicationTests.java**

```java
package com.example.scaffold;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ScaffoldApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 4: 验证项目编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/example/scaffold/module/PageController.java src/main/java/com/example/scaffold/generator/ src/test/
git commit -m "feat: add page route controller, code generator, and basic test"
```

---

### Task 10: 最终验证与收尾

- [ ] **Step 1: 运行完整测试**

Run: `mvn test`
Expected: BUILD SUCCESS, test passes

- [ ] **Step 2: 启动项目验证**

Run: `mvn spring-boot:run`
Expected: Application starts on port 8080, H2 database initializes

- [ ] **Step 3: 验证关键端点**

```bash
# 测试登录
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'

# 测试 Knife4j 文档页面
# 浏览器访问: http://localhost:8080/doc.html

# 测试 Druid 监控页面
# 浏览器访问: http://localhost:8080/druid
```

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "chore: final verification - all tests pass, application starts successfully"
```
