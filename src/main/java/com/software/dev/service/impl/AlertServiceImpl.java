package com.software.dev.service.impl;

import com.alibaba.fastjson2.JSON;
import com.software.dev.entity.AlertConfig;
import com.software.dev.entity.AlertRecord;
import com.software.dev.entity.ApiResponse;
import com.software.dev.entity.ApiTask;
import com.software.dev.mapper.AlertConfigMapper;
import com.software.dev.mapper.AlertRecordMapper;
import com.software.dev.mapper.ApiResponseMapper;
import com.software.dev.mapper.ApiTaskMapper;
import com.software.dev.service.AlertService;
import com.software.dev.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertConfigMapper alertConfigMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private ApiTaskMapper apiTaskMapper;

    @Autowired
    private ApiResponseMapper apiResponseMapper;

    @Override
    @Transactional
    public boolean saveAlertConfig(AlertConfig alertConfig) {
        try {
            // 检查是否已存在警报配置（整个系统只保留一条配置记录）
            List<AlertConfig> existingConfigs = alertConfigMapper.selectAll();
            AlertConfig existingConfig = existingConfigs.isEmpty() ? null : existingConfigs.get(0);

            if (existingConfig != null) {
                // 更新现有配置
                alertConfig.setId(existingConfig.getId());
                alertConfigMapper.update(alertConfig);
            } else {
                // 创建新配置
                alertConfig.setId(UUID.randomUUID().toString());
                alertConfig.setCreateTime(LocalDateTime.now());
                alertConfig.setUpdateTime(LocalDateTime.now());
                alertConfigMapper.insert(alertConfig);
            }

            // 更新任务的警报启用状态
            apiTaskMapper.updateTaskAlertEnabled(alertConfig.getTaskId(), alertConfig.getEnabled());

            return true;
        } catch (Exception e) {
            log.error("保存警报配置失败", e);
            return false;
        }
    }

    @Override
    public AlertConfig getAlertConfigByTaskId(String taskId) {
        return alertConfigMapper.selectByTaskId(taskId);
    }

    @Override
    public List<AlertConfig> getAllEnabledAlertConfigs() {
        return alertConfigMapper.selectAllEnabled();
    }

    @Override
    @Transactional
    public boolean enableTaskAlert(String taskId, boolean enabled) {
        try {
            // 更新任务表中的警报启用状态
            apiTaskMapper.updateTaskAlertEnabled(taskId, enabled);

            // 更新警报配置表中的启用状态
            AlertConfig config = alertConfigMapper.selectByTaskId(taskId);
            if (config != null) {
                config.setEnabled(enabled);
                config.setUpdateTime(LocalDateTime.now());
                alertConfigMapper.update(config);
            }

            return true;
        } catch (Exception e) {
            log.error("更新任务警报状态失败", e);
            return false;
        }
    }

    @Override
    @Scheduled(cron = "0 0/1 * * * *")
    public void checkAndTriggerAlerts() {
        try {
            // 获取启用警报的任务列表
            List<ApiTask> alertEnabledTasks = apiTaskMapper.findAlertEnabledTasks(true);
            
            // 获取警报配置（系统中只有一条配置记录）
            List<AlertConfig> allConfigs = alertConfigMapper.selectAll();
            if (allConfigs.isEmpty()) {
                log.info("未找到警报配置，跳过检查");
                return;
            }
            AlertConfig config = allConfigs.get(0);
            
            for (ApiTask task : alertEnabledTasks) {
                // 检查任务在检查间隔时间内的断言失败率
                checkTaskFailureRate(config, task);
            }
            
            // 更新最后检查时间
            if (!alertEnabledTasks.isEmpty()) {
                alertConfigMapper.updateLastCheckTime(config.getId());
            }
        } catch (Exception e) {
            log.error("检查警报失败", e);
        }
    }

    /**
     * 检查指定任务的失败率是否超过配置的阈值，如果超过则触发警报
     *
     * @param config 警报配置，包含检查间隔和失败率阈值等信息
     * @param task 需要检查的任务
     * @throws Exception 当查询响应记录或计算失败率过程中发生错误时抛出
     */
    private void checkTaskFailureRate(AlertConfig config, ApiTask task) {
        try {
            // 获取过去N分钟内的响应记录（N为配置的检查间隔）
            LocalDateTime timeFrom = LocalDateTime.now().minusMinutes(config.getCheckInterval());
            List<ApiResponse> responses = apiResponseMapper.findByPageWithConditions(1, 1000,
                    task.getId(), timeFrom.toString(), LocalDateTime.now().toString());

            if (responses.isEmpty()) {
                return; // 没有执行记录，不触发警报
            }

            // 计算失败率
            int totalCount = responses.size();
            long failureCount = responses.stream()
                    .filter(r -> r.getAllAssertionsPassed() != null && !r.getAllAssertionsPassed())
                    .count();

            int failureRate = totalCount > 0 ? (int) ((failureCount * 100) / totalCount) : 0;

            // 检查是否超过阈值
            if (failureRate >= config.getFailureRateThreshold()) {
                // 触发警报
                triggerAlert(config, task, failureRate, (int) failureCount, totalCount);
            }
        } catch (Exception e) {
            log.error("检查任务失败率失败", e);
        }
    }

    private void triggerAlert(AlertConfig config, ApiTask task, int failureRate, int failureCount, int totalCount) {
        try {
            // 构建警报消息
            String alertMessage = String.format(
                    "任务 [%s] 在过去%d分钟内的断言失败率为 %d%% (%d/%d)，超过阈值 %d%%",
                    task.getTaskName(), config.getCheckInterval(), failureRate, failureCount, totalCount, config.getFailureRateThreshold()
            );

            // 准备请求体
            String requestBody = config.getBody();
            if (requestBody != null && !requestBody.isEmpty()) {
                // 替换变量
                requestBody = requestBody
                        .replace("${taskId}", task.getId())
                        .replace("${taskName}", task.getTaskName())
                        .replace("${failureRate}", failureRate + "%")
                        .replace("${failureCount}", String.valueOf(failureCount))
                        .replace("${totalCount}", String.valueOf(totalCount));
            }
            
            // 准备API URL，支持变量替换
            String apiUrl = config.getApiUrl();
            if (apiUrl != null && !apiUrl.isEmpty()) {
                apiUrl = apiUrl
                        .replace("${taskId}", task.getId())
                        .replace("${taskName}", task.getTaskName())
                        .replace("${failureRate}", failureRate + "%")
                        .replace("${failureCount}", String.valueOf(failureCount))
                        .replace("${totalCount}", String.valueOf(totalCount));
            }

            // 解析请求头
            Map<String, String> headers = new HashMap<>();
            String configHeaders = config.getHeaders();
            if (configHeaders != null && !configHeaders.isEmpty()) {
                // 简单解析JSON格式的headers
                headers = parseJsonToMap(configHeaders);
            }
            
            // 解析参数
            Map<String, String> params = new HashMap<>();
            // 这里暂时没有参数需要解析，如果需要可以从config中添加参数配置

            // 发送HTTP请求
            String responseStr;
            try {
                responseStr = HttpUtil.request(apiUrl, config.getHttpMethod(), headers, params, requestBody, "application/json");
            } catch (Exception e) {
                log.error("发送警报请求失败", e);
                responseStr = "请求失败: " + e.getMessage();
            }

            // 记录警报
            AlertRecord alertRecord = new AlertRecord();
            alertRecord.setId(UUID.randomUUID().toString());
            alertRecord.setTaskId(task.getId());
            alertRecord.setTaskName(task.getTaskName());
            alertRecord.setFailureRate(failureRate);
            alertRecord.setFailureCount(failureCount);
            alertRecord.setTotalCount(totalCount);
            alertRecord.setAlertMessage(alertMessage);
            alertRecord.setApiUrl(apiUrl);
            alertRecord.setResponse(responseStr);
            alertRecord.setAlertTime(LocalDateTime.now());
            alertRecord.setCreateTime(LocalDateTime.now());

            alertRecordMapper.insert(alertRecord);

            log.info("警报已触发: {}", alertMessage);
        } catch (Exception e) {
            log.error("触发警报失败", e);
        }
    }

    private Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        try {
            // 简单的JSON解析，只处理一级键值对
            if (json == null || json.trim().isEmpty()) {
                return map;
            }

            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);

                String[] pairs = json.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", json, e);
        }
        return map;
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanOldAlertRecords() {
        try {
            // 删除30天前的警报记录
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            alertRecordMapper.deleteBeforeTime(thirtyDaysAgo);
            log.info("已清理30天前的警报记录");
        } catch (Exception e) {
            log.error("清理旧警报记录失败", e);
        }
    }

    @Override
    public List<AlertRecord> getAlertRecordsByTaskId(String taskId) {
        return alertRecordMapper.selectByTaskId(taskId);
    }
    
    @Override
    public List<AlertRecord> getAllAlertRecords(String taskName) {
        return alertRecordMapper.selectAllWithFilters(taskName);
    }
    
    @Override
    public List<AlertRecord> getAlertRecordsByPage(int page, int size, String taskName) {
        int offset = (page - 1) * size;
        return alertRecordMapper.selectByPageWithConditions(offset, size, taskName);
    }
    
    @Override
    public int countAlertRecords(String taskName) {
        return alertRecordMapper.countWithConditions(taskName);
    }
}