package com.software.dev.mapper;

import com.software.dev.entity.AlertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AlertRecordMapper {
    // 插入警报记录
    int insert(AlertRecord alertRecord);
    
    // 根据ID查询警报记录
    AlertRecord selectById(@Param("id") String id);
    
    // 根据任务ID查询警报记录
    List<AlertRecord> selectByTaskId(@Param("taskId") String taskId);
    
    // 根据时间范围查询警报记录
    List<AlertRecord> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    // 删除指定时间之前的警报记录
    int deleteBeforeTime(@Param("time") LocalDateTime time);
    
    // 查询所有警报记录，支持按任务名称筛选和按时间倒序排列
    List<AlertRecord> selectAllWithFilters(@Param("taskName") String taskName);
}