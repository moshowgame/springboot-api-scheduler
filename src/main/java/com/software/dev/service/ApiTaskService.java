package com.software.dev.service;

import com.software.dev.entity.ApiTask;
import java.util.List;

public interface ApiTaskService {
    List<ApiTask> findAll();
    
    ApiTask findById(String id);
    
    int save(ApiTask apiTask);
    
    int update(ApiTask apiTask);
    
    int deleteById(String id);
    
    List<ApiTask> findByStatus(String status);
    
    void startTask(String id);
    
    void pauseTask(String id);
    
    void executeTask(String id);
}