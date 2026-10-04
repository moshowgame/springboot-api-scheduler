package com.software.dev.controller;

import com.software.dev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/**
 * 表单登录控制器 - 供 Thymeleaf 登录页使用
 */
@Controller
@RequestMapping("/web/auth")
public class WebAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        if (userService.validateUser(username, password)) {
            session.setAttribute("user", username);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "用户名或密码错误");
        model.addAttribute("username", username);
        return "login";
    }
}
