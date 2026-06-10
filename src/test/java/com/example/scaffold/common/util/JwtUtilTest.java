package com.example.scaffold.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-unit-test");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
        jwtUtil.init();
    }

    @Test
    @DisplayName("生成 Token 应返回三段式 JWT 字符串")
    void shouldGenerateToken() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(2, token.chars().filter(c -> c == '.').count());
    }

    @Test
    @DisplayName("有效 Token 验证通过")
    void shouldVerifyValidToken() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertTrue(jwtUtil.verify(token));
    }

    @Test
    @DisplayName("伪造 Token 验证失败")
    void shouldRejectInvalidToken() {
        assertFalse(jwtUtil.verify("invalid.token.here"));
    }

    @Test
    @DisplayName("空字符串 Token 验证失败")
    void shouldRejectEmptyToken() {
        assertFalse(jwtUtil.verify(""));
    }

    @Test
    @DisplayName("过期 Token 验证失败")
    void shouldRejectExpiredToken() throws Exception {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 100L); // 100ms 过期
        jwtUtil.init();
        String token = jwtUtil.generateToken(1L, "admin");
        Thread.sleep(200); // 等待确保过期
        assertFalse(jwtUtil.verify(token));
    }

    @Test
    @DisplayName("parsePayload 正确提取 userId 和 username")
    void shouldParsePayload() {
        String token = jwtUtil.generateToken(42L, "testuser");
        JwtUtil.JwtPayload payload = jwtUtil.parsePayload(token);
        assertEquals(42L, payload.userId());
        assertEquals("testuser", payload.username());
    }

    @Test
    @DisplayName("不同用户生成不同 Token")
    void generateToken_shouldDifferPerUser() {
        assertNotEquals(
                jwtUtil.generateToken(1L, "alice"),
                jwtUtil.generateToken(2L, "bob"));
    }
}
