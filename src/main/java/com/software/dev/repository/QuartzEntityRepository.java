package com.software.dev.repository;

import com.software.dev.domain.QuartzEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuartzEntityRepository extends JpaRepository<QuartzEntity, String> {
    
    @Query(value = """
        SELECT job.JOB_NAME as jobName, job.JOB_GROUP as jobGroup, job.DESCRIPTION as description, 
               job.JOB_CLASS_NAME as jobClassName, cron.CRON_EXPRESSION as cronExpression, 
               tri.TRIGGER_NAME as triggerName, tri.TRIGGER_STATE as triggerState,
               job.JOB_NAME as oldJobName, job.JOB_GROUP as oldJobGroup
        FROM qrtz_job_details AS job 
        LEFT JOIN qrtz_triggers AS tri ON job.JOB_NAME = tri.JOB_NAME
        LEFT JOIN qrtz_cron_triggers AS cron ON cron.TRIGGER_NAME = tri.TRIGGER_NAME
        WHERE tri.TRIGGER_TYPE = 'CRON'
        """, nativeQuery = true)
    List<QuartzEntity> listQuartzEntity();
}