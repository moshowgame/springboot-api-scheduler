package com.software.dev.service;

import com.software.dev.entity.ApiResponse;
import java.util.List;

public interface ApiResponseService {
    int save(ApiResponse apiResponse);
    
    List<ApiResponse> findByTaskId(String taskId);

    List<ApiResponse> findAll();

    // 新增统一处理所有条件的方法
    List<ApiResponse> findByPageWithConditions(int page, int size, String taskId, String startTime, String endTime);
    
    int count();
    
    // 新增统一计数方法
    int countByConditions(String taskId, String startTime, String endTime);
}