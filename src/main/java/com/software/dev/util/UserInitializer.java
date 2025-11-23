package com.software.dev.util;

import com.software.dev.entity.User;
import com.software.dev.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在admin用户
        User existingUser = userMapper.findByUsername("admin");
        if (existingUser == null) {
            // 创建默认admin用户
            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setUsername("admin");
            // 密码: admin123 的MD5加密
            admin.setPassword(DigestUtils.md5DigestAsHex("admin123".getBytes()));
            admin.setEnabled(true);
            
            userMapper.insert(admin);
            System.out.println("默认用户创建成功: admin / admin123");
        }
    }
}