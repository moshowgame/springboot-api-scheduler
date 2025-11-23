package com.software.dev.service;

import com.software.dev.entity.User;

public interface UserService {
    User findByUsername(String username);
    boolean validateUser(String username, String password);
}