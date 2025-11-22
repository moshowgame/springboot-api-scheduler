package com.software.dev.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 登录校验
 * @author moshowgame
 */
@Component
public class UserLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler)throws Exception {
        HttpSession session = request.getSession(true);
        Object token = session.getAttribute("token");
        /*if(null!=token) {
            //已登录
            return true;
        }else {//未登录
            //直接重定向到登录页面
            response.sendRedirect(request.getContextPath()+"/login");
            return false;
        }*/
        return true;
    }

}