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
}