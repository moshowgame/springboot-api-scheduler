package com.software.dev.controller;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> demoGet(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // 请求信息
        response.put("method", "GET");
        response.put("url", request.getRequestURL().toString());
        response.put("queryString", request.getQueryString());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        response.put("headers", headers);
        
        // Parameters
        Map<String, String[]> parameters = request.getParameterMap();
        response.put("parameters", parameters);
        
        // Client info
        response.put("clientIP", getClientIP(request));
        response.put("userAgent", request.getHeader("User-Agent"));
        
        // Timestamp
        response.put("timestamp", System.currentTimeMillis());
        log.info("response: {}", JSON.toJSONString(response));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> demoPost(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 请求信息
        response.put("method", "POST");
        response.put("url", request.getRequestURL().toString());
        response.put("queryString", request.getQueryString());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        response.put("headers", headers);
        
        // Parameters (URL parameters)
        Map<String, String[]> parameters = request.getParameterMap();
        response.put("urlParameters", parameters);
        
        // Request Body
        response.put("requestBody", body);
        
        // Content Type
        response.put("contentType", request.getContentType());
        response.put("contentLength", request.getContentLength());
        
        // Client info
        response.put("clientIP", getClientIP(request));
        response.put("userAgent", request.getHeader("User-Agent"));
        
        // Timestamp
        response.put("timestamp", System.currentTimeMillis());
        log.info("response: {}", JSON.toJSONString(response));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echoGet(
            @RequestParam(required = false) Map<String, String> params,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET");
        response.put("url", request.getRequestURL().toString());
        response.put("headers", headers);
        response.put("parameters", params);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echoPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Map<String, String> params,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST");
        response.put("url", request.getRequestURL().toString());
        response.put("headers", headers);
        response.put("urlParameters", params);
        response.put("requestBody", body);
        response.put("contentType", request.getContentType());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}