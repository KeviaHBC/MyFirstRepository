package com.example.scaffold.module.auth.controller;

import com.example.scaffold.common.handler.GlobalExceptionHandler;
import com.example.scaffold.common.util.JwtUtil;
import com.example.scaffold.module.auth.controller.AuthController.LoginRequest;
import com.example.scaffold.module.auth.controller.AuthController.RegisterRequest;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController 集成测试")
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = mock(UserService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);

    private User mockUser;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(userService, passwordEncoder, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword("$2a$encoded");
        mockUser.setStatus(1);
    }

    @Test
    @DisplayName("登录成功 → 200 + token + username")
    void login_shouldReturnToken() throws Exception {
        when(userService.getOne(any())).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", "$2a$encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "admin")).thenReturn("fake-jwt-token");

        String body = objectMapper.writeValueAsString(
                new LoginRequest() {{ setUsername("admin"); setPassword("123456"); }});

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("用户不存在 → 500 用户名或密码错误")
    void login_userNotFound_shouldFail() throws Exception {
        when(userService.getOne(any())).thenReturn(null);

        String body = objectMapper.writeValueAsString(
                new LoginRequest() {{ setUsername("nobody"); setPassword("123456"); }});

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("密码错误 → 500")
    void login_wrongPassword_shouldFail() throws Exception {
        when(userService.getOne(any())).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", "$2a$encoded")).thenReturn(false);

        String body = objectMapper.writeValueAsString(
                new LoginRequest() {{ setUsername("admin"); setPassword("wrong"); }});

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("账号被禁用 → 500")
    void login_disabledAccount_shouldFail() throws Exception {
        mockUser.setStatus(0);
        when(userService.getOne(any())).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", "$2a$encoded")).thenReturn(true);

        String body = objectMapper.writeValueAsString(
                new LoginRequest() {{ setUsername("admin"); setPassword("123456"); }});

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.message").value("账号已被禁用"));
    }

    @Test
    @DisplayName("注册成功 → 200")
    void register_shouldSucceed() throws Exception {
        when(userService.count(any())).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$encoded");
        when(userService.save(any())).thenReturn(true);

        String body = objectMapper.writeValueAsString(
                new RegisterRequest() {{ setUsername("newuser"); setPassword("123456"); }});

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("用户名已存在 → 注册失败 500")
    void register_duplicateUsername_shouldFail() throws Exception {
        when(userService.count(any())).thenReturn(1L);

        String body = objectMapper.writeValueAsString(
                new RegisterRequest() {{ setUsername("admin"); setPassword("123456"); }});

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }
}
