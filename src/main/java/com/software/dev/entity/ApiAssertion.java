package com.software.dev.entity;

import lombok.Data;

@Data
public class ApiAssertion {
    private String id;
    private String taskId;
    private String assertionType; // HTTP_CODE, JSON_CONTAINS, JSON_PATH
    private String expectedValue;
    private String actualValue;
    private Boolean passed;
    private String errorMessage;
    private Integer sortOrder;
}