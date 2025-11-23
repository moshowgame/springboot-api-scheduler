package com.software.dev.service.impl;

import com.software.dev.entity.User;
import com.software.dev.mapper.UserMapper;
import com.software.dev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public boolean validateUser(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.getEnabled()) {
            // 简单的MD5加密验证
            String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
            return encryptedPassword.equals(user.getPassword());
        }
        return false;
    }
}