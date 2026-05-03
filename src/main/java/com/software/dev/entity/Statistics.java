package com.software.dev.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 统计数据实体类
 */
@Data
public class Statistics {
    
    // ==================== 任务统计 ====================
    /** 总任务数 */
    private Integer totalTasks;
    
    /** 运行中任务数 */
    private Integer runningTasks;
    
    /** 暂停任务数 */
    private Integer pausedTasks;
    
    // ==================== 执行统计 ====================
    /** 总执行次数 */
    private Long totalExecutions;
    
    /** 成功次数 */
    private Long successCount;
    
    /** 失败次数 */
    private Long failureCount;
    
    /** 执行成功率 */
    private Double successRate;
    
    /** 平均响应时间(ms) */
    private Long avgResponseTime;
    
    /** 最小响应时间(ms) */
    private Long minResponseTime;
    
    /** 最大响应时间(ms) */
    private Long maxResponseTime;
    
    // ==================== 警报统计 ====================
    /** 警报总数 */
    private Integer totalAlerts;
    
    /** 未读警报数 */
    private Integer unreadAlerts;
    
    // ==================== 趋势数据 ====================
    /** 每日执行趋势数据 */
    private List<DailyTrend> dailyTrends;
    
    /** 任务状态分布 */
    private List<StatusCount> statusDistribution;
    
    // ==================== 响应码统计 ====================
    /** HTTP响应码分布 */
    private Map<String, Integer> responseCodeDistribution;
    
    /**
     * 每日趋势数据
     */
    @Data
    public static class DailyTrend {
        /** 日期 */
        private String date;
        
        /** 执行次数 */
        private Integer executionCount;
        
        /** 成功次数 */
        private Integer successCount;
        
        /** 失败次数 */
        private Integer failureCount;
        
        /** 成功率 */
        private Double successRate;
        
        /** 平均响应时间 */
        private Long avgResponseTime;
    }
    
    /**
     * 状态统计数据
     */
    @Data
    public static class StatusCount {
        /** 状态 */
        private String status;
        
        /** 数量 */
        private Integer count;
    }
    
    /**
     * 任务错误统计
     */
    @Data
    public static class TaskErrorCount {
        /** 任务ID */
        private String taskId;
        
        /** 任务名称 */
        private String taskName;
        
        /** 错误次数 */
        private Long errorCount;
    }
    
    /**
     * 每日错误趋势
     */
    @Data
    public static class DailyErrorCount {
        /** 日期 */
        private String date;
        
        /** 错误次数 */
        private Integer errorCount;
    }
}
