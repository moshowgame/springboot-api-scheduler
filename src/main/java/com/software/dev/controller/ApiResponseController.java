package com.software.dev.controller;

import com.software.dev.entity.ApiResponse;
import com.software.dev.service.ApiResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/responses")
public class ApiResponseController {

    @Autowired
    private ApiResponseService apiResponseService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllResponses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String taskId) {
        
        List<ApiResponse> responses;
        int total;
        
        // 使用统一的方法处理所有筛选条件
        if (taskId != null && !taskId.isEmpty()) {
            responses = apiResponseService.findByPageWithConditions(page, size, taskId, startTime, endTime);
            total = apiResponseService.countByConditions(taskId, startTime, endTime);
        } else {
            responses = apiResponseService.findByPageWithConditions(page, size, null, startTime, endTime);
            total = apiResponseService.countByConditions(null, startTime, endTime);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", responses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("message", "Success");
        return ResponseEntity.ok(result);
    }

}