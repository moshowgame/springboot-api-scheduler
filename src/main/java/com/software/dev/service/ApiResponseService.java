package com.software.dev.service;

import com.software.dev.entity.ApiResponse;
import java.util.List;

public interface ApiResponseService {
    int save(ApiResponse apiResponse);
    
    List<ApiResponse> findByTaskId(String taskId);
    
    List<ApiResponse> findAll();
    
    List<ApiResponse> findByPage(int page, int size);
    
    int count();
}