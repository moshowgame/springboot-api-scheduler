package com.software.dev.mapper;

import com.software.dev.entity.ApiAssertion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiAssertionMapper {
    int insert(ApiAssertion apiAssertion);
    
    int update(ApiAssertion apiAssertion);
    
    List<ApiAssertion> findByTaskId(@Param("taskId") String taskId);
    
    List<ApiAssertion> findByResponseId(@Param("responseId") String responseId);
    
    int deleteByTaskId(@Param("taskId") String taskId);
}