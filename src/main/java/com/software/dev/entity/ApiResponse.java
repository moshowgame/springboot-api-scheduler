package com.software.dev.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiResponse {
    private String id;
    private String taskId;
    private String requestUrl;
    private String requestMethod;
    private String requestHeaders;
    private String requestParams;
    private Integer responseCode;
    private String responseBody;
    private Long responseTime;
    private String status;
    private String errorMessage;
    private LocalDateTime executeTime;
}