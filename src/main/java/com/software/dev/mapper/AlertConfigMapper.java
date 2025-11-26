package com.software.dev.mapper;

import com.software.dev.entity.AlertConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertConfigMapper {
    // 插入警报配置
    int insert(AlertConfig alertConfig);
    
    // 更新警报配置
    int update(AlertConfig alertConfig);
    
    // 根据ID查询警报配置
    AlertConfig selectById(@Param("id") String id);
    
    // 根据任务ID查询警报配置
    AlertConfig selectByTaskId(@Param("taskId") String taskId);
    
    // 查询所有启用的警报配置
    List<AlertConfig> selectAllEnabled();
    
    // 删除警报配置
    int deleteById(@Param("id") String id);
    
    // 删除任务相关的警报配置
    int deleteByTaskId(@Param("taskId") String taskId);
    
    // 更新任务警报启用状态
    int updateTaskAlertEnabled(@Param("taskId") String taskId, @Param("enabled") Boolean enabled);
    
    // 更新警报配置的最后检查时间
    int updateLastCheckTime(@Param("id") String id);
}