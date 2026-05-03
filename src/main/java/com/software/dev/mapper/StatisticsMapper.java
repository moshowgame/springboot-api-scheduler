package com.software.dev.mapper;

import com.software.dev.entity.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计数据Mapper
 */
@Mapper
public interface StatisticsMapper {
    
    /**
     * 获取任务统计概览
     */
    Statistics getTaskOverview();
    
    /**
     * 根据任务ID获取任务统计概览
     */
    Statistics getTaskOverviewByTaskId(@Param("taskId") String taskId);
    
    /**
     * 获取执行统计概览
     */
    Statistics getExecutionOverview(@Param("days") Integer days);
    
    /**
     * 根据任务ID获取执行统计概览
     */
    Statistics getExecutionOverviewByTaskId(@Param("days") Integer days, @Param("taskId") String taskId);
    
    /**
     * 获取警报统计
     */
    Statistics getAlertOverview();
    
    /**
     * 获取任务状态分布
     */
    List<Statistics.StatusCount> getStatusDistribution();
    
    /**
     * 根据任务ID获取任务状态分布
     */
    List<Statistics.StatusCount> getStatusDistributionByTaskId(@Param("taskId") String taskId);
    
    /**
     * 获取每日趋势数据
     */
    List<Statistics.DailyTrend> getDailyTrends(@Param("days") Integer days);
    
    /**
     * 根据任务ID获取每日趋势数据
     */
    List<Statistics.DailyTrend> getDailyTrendsByTaskId(@Param("days") Integer days, @Param("taskId") String taskId);
    
    /**
     * 获取响应码分布
     */
    List<Statistics.StatusCount> getResponseCodeDistribution(@Param("days") Integer days);
    
    /**
     * 根据任务ID获取响应码分布
     */
    List<Statistics.StatusCount> getResponseCodeDistributionByTaskId(@Param("days") Integer days, @Param("taskId") String taskId);
    
    /**
     * 获取最近N条执行记录
     */
    List<Statistics> getRecentExecutions(@Param("limit") Integer limit, @Param("days") Integer days);
    
    /**
     * 获取错误最多的任务排名 (Top 10)
     */
    List<Statistics.TaskErrorCount> getTopErrorTasks(@Param("days") Integer days, @Param("taskId") String taskId);
    
    /**
     * 获取每日错误趋势
     */
    List<Statistics.DailyErrorCount> getDailyErrorTrends(@Param("days") Integer days, @Param("taskId") String taskId);
}