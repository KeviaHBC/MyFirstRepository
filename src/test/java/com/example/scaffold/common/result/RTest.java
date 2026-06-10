package com.example.scaffold.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("R 统一响应 单元测试")
class RTest {

    @Test
    @DisplayName("ok() 返回 code=200, message=success, data=null")
    void ok_shouldSetCode200() {
        R<Void> r = R.ok();
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("ok(data) 返回 code=200 并携带数据")
    void okWithData_shouldContainData() {
        R<String> r = R.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    @DisplayName("fail(message) 返回 code=500")
    void failWithMessage_shouldSetCode500() {
        R<Void> r = R.fail("出错了");
        assertEquals(500, r.getCode());
        assertEquals("出错了", r.getMessage());
    }

    @Test
    @DisplayName("fail(code, message) 返回自定义 code 和 message")
    void failWithCode_shouldSetCustomCode() {
        R<Void> r = R.fail(400, "参数错误");
        assertEquals(400, r.getCode());
        assertEquals("参数错误", r.getMessage());
    }

    @Test
    @DisplayName("setData 后 getData 返回正确值")
    void setData_shouldWork() {
        R<Integer> r = R.ok();
        r.setData(42);
        assertEquals(42, r.getData());
    }
}
