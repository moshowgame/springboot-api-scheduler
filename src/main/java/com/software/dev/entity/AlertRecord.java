package com.software.dev.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertRecord {
    private String id;
    private String taskId;
    private String taskName;
    private Integer failureRate; // 失败率
    private Integer failureCount; // 失败次数
    private Integer totalCount; // 总执行次数
    private String alertMessage; // 警报消息
    private String apiUrl; // 调用的API地址
    private String response; // API响应结果
    private LocalDateTime alertTime; // 警报触发时间
    private LocalDateTime createTime;
}