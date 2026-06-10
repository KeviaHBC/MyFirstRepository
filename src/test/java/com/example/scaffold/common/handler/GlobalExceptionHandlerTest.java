package com.example.scaffold.common.handler;

import com.example.scaffold.common.exception.BusinessException;
import com.example.scaffold.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("BusinessException → R.fail(code, message)")
    void shouldHandleBusinessException() {
        BusinessException e = new BusinessException(401, "未授权");
        R<Void> r = handler.handleBusinessException(e);
        assertEquals(401, r.getCode());
        assertEquals("未授权", r.getMessage());
    }

    @Test
    @DisplayName("BusinessException 默认 code=500")
    void businessExceptionDefaultCode_shouldBe500() {
        BusinessException e = new BusinessException("业务错误");
        R<Void> r = handler.handleBusinessException(e);
        assertEquals(500, r.getCode());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → R.fail(400, 字段摘要)")
    void shouldHandleValidationException() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "username", "不能为空"));
        bindingResult.addError(new FieldError("test", "email", "格式不正确"));

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(
                (MethodParameter) null, bindingResult);

        R<Void> r = handler.handleValidException(e);
        assertEquals(400, r.getCode());
        assertTrue(r.getMessage().contains("username: 不能为空"));
        assertTrue(r.getMessage().contains("email: 格式不正确"));
    }

    @Test
    @DisplayName("Exception → R.fail(500, 兜底消息)")
    void shouldHandleGenericException() {
        Exception e = new Exception("未知错误");
        R<Void> r = handler.handleException(e);
        assertEquals(500, r.getCode());
        assertEquals("系统异常，请联系管理员", r.getMessage());
    }

    @Test
    @DisplayName("NoResourceFoundException → ModelAndView")
    void shouldRender404Page() {
        NoResourceFoundException e = new NoResourceFoundException(HttpMethod.GET, "/nonexistent");
        ModelAndView mv = handler.handleNoResourceFound(e);
        assertEquals("error/404", mv.getViewName());
    }
}
