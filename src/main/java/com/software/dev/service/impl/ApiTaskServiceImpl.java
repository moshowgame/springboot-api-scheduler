package com.software.dev.service.impl;

import com.software.dev.entity.ApiTask;
import com.software.dev.mapper.ApiTaskMapper;
import com.software.dev.service.ApiTaskService;
import com.software.dev.service.TaskSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApiTaskServiceImpl implements ApiTaskService {

    @Autowired
    private ApiTaskMapper apiTaskMapper;
    
    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Override
    public List<ApiTask> findAll() {
        return apiTaskMapper.findAll();
    }

    @Override
    public ApiTask findById(String id) {
        return apiTaskMapper.findById(id);
    }

    @Override
    @Transactional
    public int save(ApiTask apiTask) {
        apiTask.setId(UUID.randomUUID().toString());
        apiTask.setStatus("PAUSED");
        return apiTaskMapper.insert(apiTask);
    }

    @Override
    @Transactional
    public int update(ApiTask apiTask) {
        return apiTaskMapper.update(apiTask);
    }

    @Override
    @Transactional
    public int deleteById(String id) {
        taskSchedulerService.removeTask(id);
        return apiTaskMapper.deleteById(id);
    }

    @Override
    public List<ApiTask> findByStatus(String status) {
        return apiTaskMapper.findByStatus(status);
    }

    @Override
    @Transactional
    public void startTask(String id) {
        ApiTask task = apiTaskMapper.findById(id);
        if (task != null) {
            task.setStatus("RUNNING");
            apiTaskMapper.update(task);
            taskSchedulerService.scheduleTask(task);
        }
    }

    @Override
    @Transactional
    public void pauseTask(String id) {
        ApiTask task = apiTaskMapper.findById(id);
        if (task != null) {
            task.setStatus("PAUSED");
            apiTaskMapper.update(task);
            taskSchedulerService.removeTask(id);
        }
    }

    @Override
    public void executeTask(String id) {
        ApiTask task = apiTaskMapper.findById(id);
        if (task != null) {
            taskSchedulerService.executeTaskNow(task);
        }
    }
}