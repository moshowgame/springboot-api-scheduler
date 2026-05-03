package com.software.dev.controller;

import com.software.dev.entity.Statistics;
import com.software.dev.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计Controller
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取完整统计数据
     * @param days 最近天数，默认7天
     * @param taskId 任务ID筛选（可选）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @RequestParam(value = "taskId", required = false) String taskId) {
        Statistics statistics = statisticsService.getStatistics(days, taskId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", statistics);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取任务概览
     */
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTaskOverview() {
        Statistics statistics = statisticsService.getTaskOverview();
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", statistics);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取执行概览
     */
    @GetMapping("/executions")
    public ResponseEntity<Map<String, Object>> getExecutionOverview(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        Statistics statistics = statisticsService.getExecutionOverview(days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", statistics);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取警报概览
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlertOverview() {
        Statistics statistics = statisticsService.getAlertOverview();
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", statistics);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取任务状态分布
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<Map<String, Object>> getStatusDistribution() {
        List<Statistics.StatusCount> distribution = statisticsService.getStatusDistribution();
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", distribution);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取每日趋势
     */
    @GetMapping("/daily-trend")
    public ResponseEntity<Map<String, Object>> getDailyTrends(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        List<Statistics.DailyTrend> trends = statisticsService.getDailyTrends(days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", trends);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取响应码分布
     */
    @GetMapping("/response-codes")
    public ResponseEntity<Map<String, Object>> getResponseCodeDistribution(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        List<Statistics.StatusCount> distribution = statisticsService.getResponseCodeDistribution(days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", distribution);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取最近执行记录
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentExecutions(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        List<Statistics> executions = statisticsService.getRecentExecutions(limit, days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", executions);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取错误统计
     * @param days 最近天数
     * @param taskId 任务ID筛选（可选）
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorStatistics(
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @RequestParam(value = "taskId", required = false) String taskId) {
        Map<String, Object> errorStats = statisticsService.getErrorStatistics(days, taskId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", errorStats);
        result.put("message", "Success");
        
        return ResponseEntity.ok(result);
    }
}