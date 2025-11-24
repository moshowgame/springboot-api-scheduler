package com.software.dev.controller;

import com.software.dev.entity.AlertConfig;
import com.software.dev.entity.AlertRecord;
import com.software.dev.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alert")
public class AlertController {
    
    @Autowired
    private AlertService alertService;
    
    /**
     * 保存警报配置
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> saveAlertConfig(@RequestBody AlertConfig alertConfig) {
        try {
            boolean success = alertService.saveAlertConfig(alertConfig);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "保存警报配置成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "保存警报配置失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("保存警报配置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "保存警报配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取任务的警报配置
     */
    @GetMapping("/config/{taskId}")
    public ResponseEntity<Map<String, Object>> getAlertConfig(@PathVariable String taskId) {
        try {
            AlertConfig config = alertService.getAlertConfigByTaskId(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", config);
            response.put("message", "获取警报配置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取警报配置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取警报配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 启用任务警报
     */
    @PostMapping("/enable/{taskId}")
    public ResponseEntity<Map<String, Object>> enableAlert(@PathVariable String taskId) {
        try {
            boolean success = alertService.enableTaskAlert(taskId, true);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "启用警报成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "启用警报失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("启用警报失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "启用警报失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 禁用任务警报
     */
    @PostMapping("/disable/{taskId}")
    public ResponseEntity<Map<String, Object>> disableAlert(@PathVariable String taskId) {
        try {
            boolean success = alertService.enableTaskAlert(taskId, false);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "禁用警报成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("message", "禁用警报失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("禁用警报失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "禁用警报失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取任务的警报记录
     */
    @GetMapping("/records/{taskId}")
    public ResponseEntity<Map<String, Object>> getAlertRecords(@PathVariable String taskId) {
        try {
            List<AlertRecord> records = alertService.getAlertRecordsByTaskId(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", records);
            response.put("message", "获取警报记录成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取警报记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取警报记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}