package com.example.scaffold.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scaffold.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
