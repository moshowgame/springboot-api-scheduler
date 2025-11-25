package com.software.dev.service;

import com.software.dev.entity.ApiResponse;
import java.util.List;

public interface ApiResponseService {
    int save(ApiResponse apiResponse);
    
    List<ApiResponse> findByTaskId(String taskId);
    
    List<ApiResponse> findByTaskIdWithTimeRange(String taskId, String startTime, String endTime);
    
    List<ApiResponse> findAll();
    
    List<ApiResponse> findByPage(int page, int size);
    
    List<ApiResponse> findByPageWithTimeRange(int page, int size, String startTime, String endTime);
    
    int count();
    
    int countByTimeRange(String startTime, String endTime);
    
    int countByTaskIdWithTimeRange(String taskId, String startTime, String endTime);
}