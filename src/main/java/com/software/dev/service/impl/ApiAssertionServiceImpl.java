package com.software.dev.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.software.dev.entity.ApiAssertion;
import com.software.dev.mapper.ApiAssertionMapper;
import com.software.dev.service.ApiAssertionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ApiAssertionServiceImpl implements ApiAssertionService {

    @Autowired
    private ApiAssertionMapper apiAssertionMapper;

    @Override
    public int save(ApiAssertion apiAssertion) {
        if (apiAssertion.getId() == null) {
            apiAssertion.setId(UUID.randomUUID().toString());
        }
        return apiAssertionMapper.insert(apiAssertion);
    }
    
    @Override
    public int saveOrUpdate(ApiAssertion apiAssertion) {
        // 先查询是否已存在该任务的断言
        List<ApiAssertion> existing = findByTaskId(apiAssertion.getTaskId());
        if (existing.isEmpty()) {
            // 不存在则新增
            if (apiAssertion.getId() == null) {
                apiAssertion.setId(UUID.randomUUID().toString());
            }
            return apiAssertionMapper.insert(apiAssertion);
        } else {
            // 存在则更新
            apiAssertion.setId(existing.get(0).getId());
            return apiAssertionMapper.update(apiAssertion);
        }
    }

    @Override
    public List<ApiAssertion> findByTaskId(String taskId) {
        return apiAssertionMapper.findByTaskId(taskId);
    }

    @Override
    public List<ApiAssertion> findByResponseId(String responseId) {
        return apiAssertionMapper.findByResponseId(responseId);
    }

    @Override
    public int deleteByTaskId(String taskId) {
        return apiAssertionMapper.deleteByTaskId(taskId);
    }

    @Override
    public List<ApiAssertion> executeAssertions(String taskId, String responseBody, Integer responseCode) {
        List<ApiAssertion> assertions = findByTaskId(taskId);
        List<ApiAssertion> results = new ArrayList<>();

        for (ApiAssertion assertion : assertions) {
            ApiAssertion result = new ApiAssertion();
            result.setId(UUID.randomUUID().toString());
            result.setTaskId(taskId);
            result.setAssertionType(assertion.getAssertionType());
            result.setExpectedValue(assertion.getExpectedValue());
            result.setSortOrder(assertion.getSortOrder());

            try {
                switch (assertion.getAssertionType()) {
                    case "HTTP_CODE":
                        result.setActualValue(String.valueOf(responseCode));
                        result.setPassed(String.valueOf(responseCode).equals(assertion.getExpectedValue()));
                        if (!result.getPassed()) {
                            result.setErrorMessage("HTTP状态码不匹配: 期望 " + assertion.getExpectedValue() + ", 实际 " + responseCode);
                        }
                        break;

                    case "JSON_CONTAINS":
                        result.setActualValue(responseBody);
                        result.setPassed(responseBody != null && responseBody.contains(assertion.getExpectedValue()));
                        if (!result.getPassed()) {
                            result.setErrorMessage("响应体不包含关键字: " + assertion.getExpectedValue());
                        }
                        break;

                    case "JSON_PATH":
                        if (responseBody != null && !responseBody.isEmpty()) {
                            try {
                                Object jsonValue = JSONPath.eval(JSON.parseObject(responseBody), assertion.getExpectedValue());
                                result.setActualValue(jsonValue != null ? jsonValue.toString() : "null");
                                result.setPassed(jsonValue != null);
                                if (!result.getPassed()) {
                                    result.setErrorMessage("JSON路径不存在: " + assertion.getExpectedValue());
                                }
                            } catch (Exception e) {
                                result.setActualValue("解析错误");
                                result.setPassed(false);
                                result.setErrorMessage("JSON解析失败: " + e.getMessage());
                            }
                        } else {
                            result.setActualValue("空响应");
                            result.setPassed(false);
                            result.setErrorMessage("响应体为空，无法执行JSON路径断言");
                        }
                        break;

                    default:
                        result.setPassed(false);
                        result.setErrorMessage("不支持的断言类型: " + assertion.getAssertionType());
                        break;
                }
            } catch (Exception e) {
                result.setPassed(false);
                result.setErrorMessage("断言执行异常: " + e.getMessage());
                log.error("断言执行失败: taskId={}, type={}, error={}", taskId, assertion.getAssertionType(), e.getMessage());
            }

            results.add(result);
        }

        return results;
    }
}