package com.software.dev.service.impl;

import com.software.dev.entity.Statistics;
import com.software.dev.mapper.StatisticsMapper;
import com.software.dev.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    @Override
    public Statistics getStatistics(Integer days, String taskId) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        Statistics statistics = new Statistics();
        
        // 任务概览（如果指定了任务ID，则只统计该任务）
        Statistics taskOverview;
        if (taskId != null && !taskId.isEmpty()) {
            taskOverview = statisticsMapper.getTaskOverviewByTaskId(taskId);
        } else {
            taskOverview = statisticsMapper.getTaskOverview();
        }
        if (taskOverview != null) {
            statistics.setTotalTasks(taskOverview.getTotalTasks());
            statistics.setRunningTasks(taskOverview.getRunningTasks());
            statistics.setPausedTasks(taskOverview.getPausedTasks());
        }
        
        // 执行概览（根据任务ID筛选）
        Statistics executionOverview;
        if (taskId != null && !taskId.isEmpty()) {
            executionOverview = statisticsMapper.getExecutionOverviewByTaskId(days, taskId);
        } else {
            executionOverview = statisticsMapper.getExecutionOverview(days);
        }
        if (executionOverview != null) {
            statistics.setTotalExecutions(executionOverview.getTotalExecutions());
            statistics.setSuccessCount(executionOverview.getSuccessCount());
            statistics.setFailureCount(executionOverview.getFailureCount());
            statistics.setAssertionSuccessCount(executionOverview.getAssertionSuccessCount());
            statistics.setAssertionFailureCount(executionOverview.getAssertionFailureCount());
            statistics.setAvgResponseTime(executionOverview.getAvgResponseTime());
            statistics.setMinResponseTime(executionOverview.getMinResponseTime());
            statistics.setMaxResponseTime(executionOverview.getMaxResponseTime());
            
            // 计算成功率
            Long total = executionOverview.getTotalExecutions();
            Long success = executionOverview.getSuccessCount();
            if (total != null && total > 0 && success != null) {
                statistics.setSuccessRate(Math.round(success * 10000.0 / total) / 100.0);
            }
        }
        
        // 警报概览
        Statistics alertOverview = statisticsMapper.getAlertOverview();
        if (alertOverview != null) {
            statistics.setTotalAlerts(alertOverview.getTotalAlerts());
        }
        
        // 状态分布（指定任务时显示单个任务状态）
        if (taskId != null && !taskId.isEmpty()) {
            statistics.setStatusDistribution(statisticsMapper.getStatusDistributionByTaskId(taskId));
        } else {
            statistics.setStatusDistribution(statisticsMapper.getStatusDistribution());
        }
        
        // 每日趋势（根据任务ID筛选）
        if (taskId != null && !taskId.isEmpty()) {
            statistics.setDailyTrends(statisticsMapper.getDailyTrendsByTaskId(days, taskId));
        } else {
            statistics.setDailyTrends(statisticsMapper.getDailyTrends(days));
        }
        
        // 响应码分布（根据任务ID筛选）
        if (taskId != null && !taskId.isEmpty()) {
            statistics.setResponseCodeDistribution(
                convertToMap(statisticsMapper.getResponseCodeDistributionByTaskId(days, taskId))
            );
        } else {
            statistics.setResponseCodeDistribution(
                convertToMap(statisticsMapper.getResponseCodeDistribution(days))
            );
        }
        
        return statistics;
    }
    
    @Override
    public Statistics getTaskOverview() {
        return statisticsMapper.getTaskOverview();
    }
    
    @Override
    public Statistics getExecutionOverview(Integer days) {
        return statisticsMapper.getExecutionOverview(days);
    }
    
    @Override
    public Statistics getAlertOverview() {
        return statisticsMapper.getAlertOverview();
    }
    
    @Override
    public List<Statistics.StatusCount> getStatusDistribution() {
        return statisticsMapper.getStatusDistribution();
    }
    
    @Override
    public List<Statistics.DailyTrend> getDailyTrends(Integer days) {
        return statisticsMapper.getDailyTrends(days);
    }
    
    @Override
    public List<Statistics.StatusCount> getResponseCodeDistribution(Integer days) {
        return statisticsMapper.getResponseCodeDistribution(days);
    }
    
    @Override
    public List<Statistics> getRecentExecutions(Integer limit, Integer days) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (days == null || days <= 0) {
            days = 7;
        }
        return statisticsMapper.getRecentExecutions(limit, days);
    }
    
    /**
     * 转换为Map
     */
    private java.util.Map<String, Integer> convertToMap(List<Statistics.StatusCount> list) {
        if (list == null || list.isEmpty()) {
            return new java.util.HashMap<>();
        }
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        for (Statistics.StatusCount item : list) {
            map.put(item.getStatus(), item.getCount());
        }
        return map;
    }
    
    @Override
    public Map<String, Object> getErrorStatistics(Integer days, String taskId) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 获取错误任务排名 (Top 10)
        List<Statistics.TaskErrorCount> taskErrors = statisticsMapper.getTopErrorTasks(days, taskId);
        result.put("taskErrors", taskErrors);
        
        // 获取每日错误趋势
        List<Statistics.DailyErrorCount> dailyErrors = statisticsMapper.getDailyErrorTrends(days, taskId);
        result.put("dailyErrors", dailyErrors);
        
        // 错误总数
        long totalErrors = taskErrors.stream().mapToLong(Statistics.TaskErrorCount::getErrorCount).sum();
        result.put("totalErrors", totalErrors);
        
        return result;
    }
}