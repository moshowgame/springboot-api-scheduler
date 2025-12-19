package com.software.dev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 页面控制器 - 提供Vue应用的SPA入口
 * 
 * 注意：这是一个Vue 3单页应用，所有前端路由都通过Vue Router处理。
 * Spring Boot只需要提供主HTML文件，其他路由由前端处理。
 */
@Controller
@RequestMapping
public class PageController {

    /**
     * 主页面 - Vue应用的入口点
     * 所有前端路由都通过Vue Router处理
     * 包括: /dashboard, /tasks, /monitoring, /alerts, /assertions, /test, /profile 等
     */
    @GetMapping(value = {"/", "/dashboard", "/tasks", "/monitoring", "/alerts", "/assertions", "/test", "/profile"})
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * 捕获所有其他前端路由，返回Vue应用
     * 这样可以确保前端路由刷新时不会出现404错误
     */
    @GetMapping(value = {"/404", "/**/{path:[^\\.]*}"})
    public String catchAll() {
        return "index";
    }
}