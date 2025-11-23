package com.software.dev.service;

import com.software.dev.entity.ApiAssertion;
import java.util.List;

public interface ApiAssertionService {
    int save(ApiAssertion apiAssertion);
    int saveOrUpdate(ApiAssertion apiAssertion);
    List<ApiAssertion> findByTaskId(String taskId);
    List<ApiAssertion> findByResponseId(String responseId);
    int deleteByTaskId(String taskId);
    List<ApiAssertion> executeAssertions(String taskId, String responseBody, Integer responseCode);
}