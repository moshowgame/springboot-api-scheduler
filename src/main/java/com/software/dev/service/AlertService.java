package com.software.dev.service;

import com.software.dev.entity.AlertConfig;
import com.software.dev.entity.AlertRecord;

import java.util.List;

public interface AlertService {
    // 保存警报配置
    boolean saveAlertConfig(AlertConfig alertConfig);
    
    // 根据任务ID获取警报配置
    AlertConfig getAlertConfigByTaskId(String taskId);
    
    // 获取所有启用的警报配置
    List<AlertConfig> getAllEnabledAlertConfigs();
    
    // 启用/禁用任务警报
    boolean enableTaskAlert(String taskId, boolean enabled);
    
    // 检查并触发警报
    void checkAndTriggerAlerts();
    
    // 获取任务的警报记录
    List<AlertRecord> getAlertRecordsByTaskId(String taskId);
    
    // 获取所有警报记录，支持筛选
    List<AlertRecord> getAllAlertRecords(String taskName);
    
    // 清理旧的警报记录
    void cleanOldAlertRecords();
}