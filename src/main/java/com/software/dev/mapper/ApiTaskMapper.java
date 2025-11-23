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
}