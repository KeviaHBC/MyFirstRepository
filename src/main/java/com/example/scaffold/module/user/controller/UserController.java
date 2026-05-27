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
