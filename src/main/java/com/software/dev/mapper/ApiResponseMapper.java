package com.software.dev.mapper;

import com.software.dev.entity.ApiResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ApiResponseMapper {
    int insert(ApiResponse apiResponse);
    
    List<ApiResponse> findByTaskId(@Param("taskId") String taskId);
    
    List<ApiResponse> findByTaskIdWithTimeRange(@Param("taskId") String taskId, 
                                               @Param("startTime") String startTime, 
                                               @Param("endTime") String endTime);
    
    List<ApiResponse> findAll();
    
    List<ApiResponse> findByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    List<ApiResponse> findByPageWithTimeRange(@Param("offset") int offset, 
                                             @Param("limit") int limit, 
                                             @Param("startTime") String startTime, 
                                             @Param("endTime") String endTime);
    
    int count();
    
    int countByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    int countByTaskIdWithTimeRange(@Param("taskId") String taskId, 
                                   @Param("startTime") String startTime, 
                                   @Param("endTime") String endTime);
}