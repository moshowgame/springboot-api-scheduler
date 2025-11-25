package com.software.dev.service.impl;

import com.software.dev.entity.ApiResponse;
import com.software.dev.mapper.ApiResponseMapper;
import com.software.dev.service.ApiResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
    public List<ApiResponse> findByTaskIdWithTimeRange(String taskId, String startTime, String endTime) {
        return apiResponseMapper.findByTaskIdWithTimeRange(taskId, startTime, endTime);
    }

    @Override
    public List<ApiResponse> findAll() {
        return apiResponseMapper.findAll();
    }

    @Override
    public List<ApiResponse> findByPage(int page, int size) {
        int offset = (page - 1) * size;
        return apiResponseMapper.findByPage(offset, size);
    }

    @Override
    public List<ApiResponse> findByPageWithTimeRange(int page, int size, String startTime, String endTime) {
        int offset = (page - 1) * size;
        return apiResponseMapper.findByPageWithTimeRange(offset, size, startTime, endTime);
    }

    @Override
    public int count() {
        return apiResponseMapper.count();
    }

    @Override
    public int countByTimeRange(String startTime, String endTime) {
        return apiResponseMapper.countByTimeRange(startTime, endTime);
    }

    @Override
    public int countByTaskIdWithTimeRange(String taskId, String startTime, String endTime) {
        return apiResponseMapper.countByTaskIdWithTimeRange(taskId, startTime, endTime);
    }
}