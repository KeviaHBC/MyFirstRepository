package com.example.scaffold.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessException 单元测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("单参构造应设置 message，code 默认为 500")
    void constructor1_shouldSetMessageAndDefaultCode() {
        BusinessException e = new BusinessException("用户名已存在");
        assertEquals("用户名已存在", e.getMessage());
        assertEquals(500, e.getCode());
    }

    @Test
    @DisplayName("双参构造应设置 code 和 message")
    void constructor2_shouldSetCodeAndMessage() {
        BusinessException e = new BusinessException(401, "未授权");
        assertEquals(401, e.getCode());
        assertEquals("未授权", e.getMessage());
    }

    @Test
    @DisplayName("异常应可被捕获为 RuntimeException")
    void shouldBeRuntimeException() {
        BusinessException e = new BusinessException("test");
        assertInstanceOf(RuntimeException.class, e);
    }
}
