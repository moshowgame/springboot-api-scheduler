package com.software.dev.service.impl;

import com.alibaba.fastjson2.JSON;
import com.software.dev.entity.ApiResponse;
import com.software.dev.entity.ApiTask;
import com.software.dev.mapper.ApiTaskMapper;
import com.software.dev.service.ApiResponseService;
import com.software.dev.service.TaskSchedulerService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class TaskSchedulerServiceImpl implements TaskSchedulerService {

    @Autowired
    private TaskScheduler taskScheduler;
    
    @Autowired
    private ApiTaskMapper apiTaskMapper;
    
    @Autowired
    private ApiResponseService apiResponseService;
    
    private final OkHttpClient httpClient;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public TaskSchedulerServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void scheduleTask(ApiTask task) {
        try {
            CronTrigger cronTrigger = new CronTrigger(task.getCronExpression());
            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                () -> executeTask(task), 
                cronTrigger
            );
            scheduledTasks.put(task.getId(), scheduledFuture);
            log.info("Task scheduled: {} with cron: {}", task.getTaskName(), task.getCronExpression());
        } catch (Exception e) {
            log.error("Failed to schedule task: {}", task.getTaskName(), e);
        }
    }

    @Override
    public void removeTask(String taskId) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(taskId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            log.info("Task removed: {}", taskId);
        }
    }

    @Override
    public void executeTaskNow(ApiTask task) {
        executeTask(task);
    }

    @Override
    public void loadAllTasks() {
        var tasks = apiTaskMapper.findByStatus("RUNNING");
        for (ApiTask task : tasks) {
            scheduleTask(task);
        }
        log.info("Loaded {} running tasks", tasks.size());
    }

    private void executeTask(ApiTask task) {
        log.info("Executing task: {}", task.getTaskName());
        
        ApiResponse response = new ApiResponse();
        response.setTaskId(task.getId());
        response.setRequestUrl(task.getUrl());
        response.setRequestMethod(task.getMethod());
        response.setRequestHeaders(task.getHeaders());
        response.setRequestParams(task.getParameters());
        response.setExecuteTime(LocalDateTime.now());
        
        long startTime = System.currentTimeMillis();
        
        try {
            Request.Builder requestBuilder = new Request.Builder().url(task.getUrl());
            
            // Set method
            switch (task.getMethod().toUpperCase()) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                    RequestBody body = RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
                    requestBuilder.post(body);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + task.getMethod());
            }
            
            // Set headers
            if (task.getHeaders() != null && !task.getHeaders().isEmpty()) {
                try {
                    Map<String, String> headers = JSON.parseObject(task.getHeaders(), Map.class);
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        requestBuilder.addHeader(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse headers for task: {}", task.getTaskName(), e);
                }
            }
            
            // Set timeout
            int timeout = task.getTimeout() != null ? task.getTimeout() : 30000;
            
            Request request = requestBuilder.build();
            
            try (Response httpResponse = httpClient.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                response.setResponseTime(endTime - startTime);
                response.setResponseCode(httpResponse.code());
                response.setResponseBody(httpResponse.body() != null ? httpResponse.body().string() : "");
                response.setStatus("SUCCESS");
            }
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            response.setResponseTime(endTime - startTime);
            response.setStatus("ERROR");
            response.setErrorMessage(e.getMessage());
            log.error("Task execution failed: {}", task.getTaskName(), e);
        }
        
        apiResponseService.save(response);
        log.info("Task execution completed: {} - Status: {}", task.getTaskName(), response.getStatus());
    }
}