package com.software.dev.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiTask {
    private String id;
    private String taskName;
    private String url;
    private String method;
    private Integer timeout;
    private String headers;
    private String parameters;
    private String cronExpression;
    private String status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastExecuteTime;
    private String assertions; // 断言配置JSON
    private Boolean alertEnabled; // 警报是否启用

    /** 任务触发类型: CRON=定时调度（默认）/ CHAIN=被链式调用（不参与定时调度） */
    private String triggerType;
    /** 本任务执行完成后要触发的下游任务 id */
    private String nextTaskId;
    /** 触发下游任务的条件: ALWAYS=任意 / ASSERTION_PASS=断言成功 / ASSERTION_FAIL=断言失败 */
    private String triggerCondition;

    public boolean isChainTriggered() {
        return "CHAIN".equals(triggerType);
    }
}