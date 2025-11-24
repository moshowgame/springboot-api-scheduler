package com.software.dev.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertConfig {
    private String id;
    private String taskId;
    private Integer failureRateThreshold; // 失败率阈值(%)
    private Integer checkInterval; // 检查间隔(分钟)
    private String apiUrl; // 警报API地址
    private String httpMethod; // 请求方法
    private String headers; // 请求头(JSON格式)
    private String body; // 请求体(JSON格式)
    private Boolean enabled; // 是否启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastCheckTime; // 上次检查时间
}