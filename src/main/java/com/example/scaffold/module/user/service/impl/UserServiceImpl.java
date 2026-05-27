package com.example.scaffold.module.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scaffold.module.user.entity.User;
import com.example.scaffold.module.user.mapper.UserMapper;
import com.example.scaffold.module.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
