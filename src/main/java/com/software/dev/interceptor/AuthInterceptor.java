package com.software.dev.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 允许访问的路径
        if (requestURI.startsWith("/demo/") || 
            requestURI.equals("/api/auth/login") || 
            requestURI.equals("/api/auth/check") ||
            requestURI.equals("/login") ||
            requestURI.equals("/")) {
            return true;
        }
        
        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return true;
        }
        
        // 未登录，返回401状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\": false, \"message\": \"请先登录\"}");
        return false;
    }
}