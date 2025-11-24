package com.software.dev.mapper;

import com.software.dev.entity.ApiResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ApiResponseMapper {
    int insert(ApiResponse apiResponse);
    
    List<ApiResponse> findByTaskId(@Param("taskId") String taskId);
    
    List<ApiResponse> findAll();
    
    List<ApiResponse> findByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    int count();
    
    // 根据任务ID和时间范围查询响应记录
    List<ApiResponse> selectByTaskIdAndTimeRange(@Param("taskId") String taskId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
}