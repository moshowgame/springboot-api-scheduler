package com.software.dev.controller;

import com.software.dev.entity.User;
import com.software.dev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (userService.validateUser(username, password)) {
            session.setAttribute("user", username);
            response.put("success", true);
            response.put("message", "登录成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.removeAttribute("user");
        session.invalidate();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "退出成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(HttpSession session) {
        String user = (String) session.getAttribute("user");
        
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("authenticated", true);
            response.put("username", user);
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }
}