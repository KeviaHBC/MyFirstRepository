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
