package com.software.dev.controller;

import com.software.dev.entity.ApiAssertion;
import com.software.dev.entity.ApiTask;
import com.software.dev.mapper.ApiTaskMapper;
import com.software.dev.service.ApiAssertionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/assertions")
public class ApiAssertionController {

    @Autowired
    private ApiAssertionService apiAssertionService;
    
    @Autowired
    private ApiTaskMapper apiTaskMapper;

    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getAssertionsByTaskId(@PathVariable String taskId) {
        try {
            List<ApiAssertion> assertions = apiAssertionService.findByTaskId(taskId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", assertions);
            result.put("message", "Success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取任务断言失败: taskId={}", taskId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取任务断言失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/response/{responseId}")
    public ResponseEntity<Map<String, Object>> getAssertionsByResponseId(@PathVariable String responseId) {
        try {
            List<ApiAssertion> assertions = apiAssertionService.findByResponseId(responseId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", assertions);
            result.put("message", "Success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取响应断言失败: responseId={}", responseId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取响应断言失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> saveAssertion(@PathVariable String taskId, @RequestBody ApiAssertion assertion) {
        try {
            // 验证任务是否存在
            ApiTask task = apiTaskMapper.findById(taskId);
            if (task == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 404);
                result.put("message", "任务不存在");
                return ResponseEntity.status(404).body(result);
            }
            
            assertion.setTaskId(taskId);
            
            int result = apiAssertionService.saveOrUpdate(assertion);
            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "断言保存成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "断言保存失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("保存断言失败: taskId={}", taskId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "保存断言失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteAssertionsByTaskId(@PathVariable String taskId) {
        try {
            int result = apiAssertionService.deleteByTaskId(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功，共删除 " + result + " 条断言");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除任务断言失败: taskId={}", taskId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "删除任务断言失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/task/{taskId}/batch")
    public ResponseEntity<Map<String, Object>> batchAddAssertions(@PathVariable String taskId, @RequestBody List<ApiAssertion> assertions) {
        try {
            // 验证任务是否存在
            ApiTask task = apiTaskMapper.findById(taskId);
            if (task == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 404);
                result.put("message", "任务不存在");
                return ResponseEntity.status(404).body(result);
            }
            
            // 先删除原有断言
            apiAssertionService.deleteByTaskId(taskId);
            
            // 添加新断言
            int successCount = 0;
            for (int i = 0; i < assertions.size(); i++) {
                ApiAssertion assertion = assertions.get(i);
                assertion.setId(UUID.randomUUID().toString());
                assertion.setTaskId(taskId);
                assertion.setSortOrder(i);
                
                try {
                    apiAssertionService.save(assertion);
                    successCount++;
                } catch (Exception e) {
                    log.error("添加断言失败: {}", assertion, e);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "批量添加完成，成功添加 " + successCount + " 条断言");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量添加断言失败: taskId={}", taskId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "批量添加断言失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}