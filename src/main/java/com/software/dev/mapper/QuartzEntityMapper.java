package com.software.dev.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.dev.domain.QuartzEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface  QuartzEntityMapper extends BaseMapper<QuartzEntity> {

/**
 * query the quartz list
 */
@Select("""
        SELECT job.JOB_NAME as jobName,job.JOB_GROUP as jobGroup,job.DESCRIPTION as description,job.JOB_CLASS_NAME as jobClassName,
        cron.CRON_EXPRESSION as cronExpression,tri.TRIGGER_NAME as triggerName,tri.TRIGGER_STATE as triggerState,
        job.JOB_NAME as oldJobName,job.JOB_GROUP as oldJobGroup
        FROM qrtz_job_details AS job LEFT JOIN qrtz_triggers AS tri ON job.JOB_NAME = tri.JOB_NAME
        LEFT JOIN qrtz_cron_triggers AS cron ON cron.TRIGGER_NAME = tri.TRIGGER_NAME
        WHERE tri.TRIGGER_TYPE = 'CRON' 
        """)
 List<QuartzEntity> listQuartzEntity();
}
