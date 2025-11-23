package com.software.dev.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * @author zhengkai
 */
@Entity
@Data
public class QuartzEntity {
    @Id
    private Long id;
    private String jobName;
    private String jobGroup = "DEFAULT";
    private String description;
    private String jobClassName;
    private String cronExpression;
    private String triggerName;
    private String triggerState;
    private String oldJobName;
    private String oldJobGroup = "DEFAULT";
}
