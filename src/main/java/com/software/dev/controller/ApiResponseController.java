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
            @RequestParam(defaultValue = "10") int size) {
        List<ApiResponse> responses = apiResponseService.findByPage(page, size);
        int total = apiResponseService.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", responses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("message", "Success");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getResponsesByTaskId(@PathVariable String taskId) {
        List<ApiResponse> responses = apiResponseService.findByTaskId(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", responses);
        result.put("message", "Success");
        return ResponseEntity.ok(result);
    }
}