package com.software.dev.service.impl;

import com.alibaba.fastjson2.JSON;
import com.software.dev.entity.ApiAssertion;
import com.software.dev.entity.ApiResponse;
import com.software.dev.entity.ApiTask;
import com.software.dev.mapper.ApiTaskMapper;
import com.software.dev.service.ApiAssertionService;
import com.software.dev.service.ApiResponseService;
import com.software.dev.service.TaskSchedulerService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
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
    
    @Autowired
    private ApiAssertionService apiAssertionService;
    
    private final OkHttpClient httpClient;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 创建一个不验证证书的HttpClient
     * @author Moshow
     */
    public TaskSchedulerServiceImpl() {
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        //nothing to verify
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        //nothing to verify
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
        SSLContext sslContext = null;
        {
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        this.httpClient = builder
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

    /**
     * 构建带参数的URL
     * @param baseUrl 基础URL
     * @param params 参数Map
     * @return 带参数的完整URL
     */
    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean hasQuery = baseUrl.contains("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                if (!hasQuery) {
                    urlBuilder.append("?");
                    hasQuery = true;
                } else {
                    urlBuilder.append("&");
                }
                urlBuilder.append(key).append("=").append(value);
            } catch (Exception e) {
                log.warn("Failed to encode URL parameter: {}={}", entry.getKey(), entry.getValue(), e);
            }
        }
        return urlBuilder.toString();
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
            // 解析参数
            Map<String, String> params = null;
            if (task.getParameters() != null && !task.getParameters().isEmpty()) {
                try {
                    params = JSON.parseObject(task.getParameters(), Map.class);
                } catch (Exception e) {
                    log.warn("Failed to parse parameters for task: {}", task.getTaskName(), e);
                }
            }
            
            // 构建带参数的URL（仅对GET请求）
            String requestUrl = task.getUrl();
            if ("GET".equalsIgnoreCase(task.getMethod()) && params != null && !params.isEmpty()) {
                requestUrl = buildUrlWithParams(task.getUrl(), params);
            }
            
            Request.Builder requestBuilder = new Request.Builder().url(requestUrl);
            
            // Set method
            switch (task.getMethod().toUpperCase()) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                    // POST请求使用请求体
                    String postBody = task.getParameters();
                    RequestBody body = RequestBody.create(postBody != null ? postBody : "", MediaType.parse("application/json; charset=utf-8"));
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
        
        // 执行断言
        try {
            List<ApiAssertion> assertionResults = apiAssertionService.executeAssertions(
                task.getId(), 
                response.getResponseBody(), 
                response.getResponseCode()
            );
            
            if (!assertionResults.isEmpty()) {

                // 汇总断言结果
                boolean allPassed = assertionResults.stream().allMatch(ApiAssertion::getPassed);
                response.setAllAssertionsPassed(allPassed);
                
                StringBuilder resultSummary = new StringBuilder();
                resultSummary.append("断言总数: ").append(assertionResults.size());
                resultSummary.append(", 通过: ").append(assertionResults.stream().mapToInt(a -> a.getPassed() ? 1 : 0).sum());
                resultSummary.append(", 失败: ").append(assertionResults.stream().mapToInt(a -> !a.getPassed() ? 1 : 0).sum());
                
                if (!allPassed) {
                    resultSummary.append(" | 失败原因: ");
                    assertionResults.stream()
                        .filter(a -> !a.getPassed() && a.getErrorMessage() != null)
                        .findFirst()
                        .ifPresent(a -> resultSummary.append(a.getErrorMessage()));
                }
                
                response.setAssertionResult(resultSummary.toString());
                
                log.info("断言执行完成 - 任务: {}, 结果: {}", task.getTaskName(), resultSummary.toString());
            }
        } catch (Exception e) {
            log.error("断言执行异常 - 任务: {}", task.getTaskName(), e);
            response.setAssertionResult("断言执行异常: " + e.getMessage());
            response.setAllAssertionsPassed(false);
        }
        
        apiResponseService.save(response);
        log.info("Task execution completed: {} - Status: {} - Assertions: {}", 
            task.getTaskName(), response.getStatus(), response.getAssertionResult());
    }
}