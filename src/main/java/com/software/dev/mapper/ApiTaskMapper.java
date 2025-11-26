package com.software.dev.mapper;

import com.software.dev.entity.ApiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiTaskMapper {
    List<ApiTask> findAll();
    
    ApiTask findById(@Param("id") String id);
    
    int insert(ApiTask apiTask);
    
    int update(ApiTask apiTask);
    
    int deleteById(@Param("id") String id);
    
    List<ApiTask> findByStatus(@Param("status") String status);
    
    // 查找启用警报的任务
    List<ApiTask> findAlertEnabledTasks(@Param("alertEnabled") Boolean alertEnabled);
    
    // 更新任务警报启用状态
    int updateTaskAlertEnabled(@Param("taskId") String taskId, @Param("enabled") Boolean enabled);
}