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

    List<ApiResponse> findByPageWithConditions(@Param("offset") int offset,
                                               @Param("limit") int limit,
                                               @Param("taskId") String taskId,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime);
    int countByConditions(@Param("taskId") String taskId,
                          @Param("startTime") String startTime,
                          @Param("endTime") String endTime);

    int count();

}