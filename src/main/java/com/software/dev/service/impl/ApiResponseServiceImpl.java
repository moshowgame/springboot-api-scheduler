package com.software.dev.service.impl;

import com.software.dev.entity.ApiResponse;
import com.software.dev.mapper.ApiResponseMapper;
import com.software.dev.service.ApiResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ApiResponseServiceImpl implements ApiResponseService {

    @Autowired
    private ApiResponseMapper apiResponseMapper;

    @Override
    public int save(ApiResponse apiResponse) {
        apiResponse.setId(UUID.randomUUID().toString());
        return apiResponseMapper.insert(apiResponse);
    }

    @Override
    public List<ApiResponse> findByTaskId(String taskId) {
        return apiResponseMapper.findByTaskId(taskId);
    }

    @Override
    public ApiResponse findById(String id) {
        return apiResponseMapper.findById(id);
    }


    @Override
    public List<ApiResponse> findAll() {
        return apiResponseMapper.findAll();
    }



    @Override
    public List<ApiResponse> findByPageWithConditions(int page, int size, String taskId, String startTime, String endTime) {
        int offset = (page - 1) * size;
        return apiResponseMapper.findByPageWithConditions(offset, size, taskId, startTime, endTime);
    }

    @Override
    public int count() {
        return apiResponseMapper.count();
    }


    @Override
    public int countByConditions(String taskId, String startTime, String endTime) {
        return apiResponseMapper.countByConditions(taskId, startTime, endTime);
    }

    /**
     * 定时清理 30 天前的响应记录，防止 api_response 表因响应体全量入库而无限增长。
     * 每天凌晨 2 点执行，保留策略与警报记录（30 天）保持一致。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredResponses() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            int deleted = apiResponseMapper.deleteBeforeTime(threshold);
            if (deleted > 0) {
                log.info("已清理 {} 条 {} 之前的响应记录", deleted, threshold);
            }
        } catch (Exception e) {
            log.error("清理过期响应记录失败", e);
        }
    }
}