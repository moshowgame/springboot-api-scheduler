package com.software.dev.mapper;

import com.software.dev.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    int insert(User user);
    int update(User user);
    int deleteById(String id);
}