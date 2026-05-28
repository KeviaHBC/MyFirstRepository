package com.example.scaffold.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scaffold.common.annotation.Log;
import com.example.scaffold.common.result.R;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public R<Page<User>> page(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                               @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
                               @Parameter(description = "用户名") @RequestParam(required = false) String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return R.ok(userService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public R<User> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @Log("新增用户")
    @PostMapping
    public R<Void> save(@Valid @RequestBody User user) {
        userService.save(user);
        return R.ok();
    }

    @Operation(summary = "更新用户")
    @Log("更新用户")
    @PutMapping
    public R<Void> update(@Valid @RequestBody User user) {
        userService.updateById(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @Log("删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }
}
