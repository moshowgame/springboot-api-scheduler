package com.software.dev.service;

import com.software.dev.entity.ApiTask;

public interface TaskSchedulerService {
    void scheduleTask(ApiTask task);
    
    void removeTask(String taskId);
    
    void executeTaskNow(ApiTask task);
    
    void loadAllTasks();
}