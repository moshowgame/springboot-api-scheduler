package com.software.dev.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 登录拦截器
 * <p>
 * 对未登录请求的处理策略：
 * 1. HTMX 请求（携带 HX-Request 头）返回 401 并通过 HX-Redirect 让浏览器跳转登录页；
 * 2. 浏览器页面请求（Accept 含 text/html）直接重定向到 /login；
 * 3. 其余（REST API）返回 401 JSON。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 允许匿名访问的路径
        if (isPublicPath(requestURI)) {
            return true;
        }

        // 已登录直接放行
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return true;
        }

        // 未登录：HTMX 请求
        if ("true".equals(request.getHeader("HX-Request"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("HX-Redirect", "/login");
            return false;
        }

        // 未登录：浏览器页面请求 -> 跳转登录页
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            response.sendRedirect("/login");
            return false;
        }

        // 未登录：接口请求 -> 401 JSON
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\": false, \"message\": \"请先登录\"}");
        return false;
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("/demo/")
                || uri.startsWith("/api/auth/")
                || uri.startsWith("/web/auth/")
                || uri.equals("/login")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.equals("/favicon.ico");
    }
}
