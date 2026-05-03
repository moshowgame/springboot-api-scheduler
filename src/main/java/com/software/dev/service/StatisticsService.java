package com.software.dev.service;

import com.software.dev.entity.Statistics;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取完整统计数据
     * @param days 最近天数
     * @param taskId 任务ID筛选（可选）
     * @return 统计数据
     */
    Statistics getStatistics(Integer days, String taskId);
    
    /**
     * 获取任务概览
     */
    Statistics getTaskOverview();
    
    /**
     * 获取执行概览
     */
    Statistics getExecutionOverview(Integer days);
    
    /**
     * 获取警报概览
     */
    Statistics getAlertOverview();
    
    /**
     * 获取任务状态分布
     */
    List<Statistics.StatusCount> getStatusDistribution();
    
    /**
     * 获取每日趋势
     */
    List<Statistics.DailyTrend> getDailyTrends(Integer days);
    
    /**
     * 获取响应码分布
     */
    List<Statistics.StatusCount> getResponseCodeDistribution(Integer days);
    
    /**
     * 获取最近执行记录
     * @param limit 限制数量
     * @param days 最近天数
     */
    List<Statistics> getRecentExecutions(Integer limit, Integer days);
    
    /**
     * 获取错误统计
     * @param days 最近天数
     * @param taskId 任务ID筛选（可选）
     * @return 错误统计数据
     */
    Map<String, Object> getErrorStatistics(Integer days, String taskId);
}