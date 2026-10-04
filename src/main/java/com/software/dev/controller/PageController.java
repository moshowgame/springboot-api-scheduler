package com.software.dev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

/**
 * 页面控制器 - Thymeleaf 服务端渲染入口
 * <p>
 * 各业务页面的完整渲染与 HTMX 片段由对应的 XxxPageController 提供。
 */
@Controller
public class PageController {

    /** 首页重定向到数据统计 */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /** 登录页 */
    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /** 退出登录 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        session.invalidate();
        return "redirect:/login";
    }
}
