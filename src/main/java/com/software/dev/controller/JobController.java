package com.software.dev.controller;

import com.software.dev.domain.QuartzEntity;
import com.software.dev.domain.Result;
import com.software.dev.mapper.QuartzEntityMapper;
import com.software.dev.mapper.UrlRequestMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * Job Controller
 * Quartz原生的任务控制器
 * @author zhengkai@blog.csdn.net/moshowgame
 * @date 2019/03/10
 */
@RestController
@Slf4j
@RequestMapping("/oldJob")
public class JobController {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private QuartzEntityMapper quartzEntityMapper;
    @Autowired
    private UrlRequestMapper urlRequestMapper;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PostMapping("/add")
    public Result add(QuartzEntity quartz,String requestId){
        log.info("新增任务");
        quartz.setJobClassName("com.software.dev.job.UrlJob");
        try {
            //如果存在旧的，删除重新添加
            if(quartz.getOldJobGroup()!=null){
                JobKey key = new JobKey(quartz.getJobName(), quartz.getJobGroup());
                scheduler.deleteJob(key);
            }
            Class cls = Class.forName(quartz.getJobClassName()) ;
            cls.newInstance();
            //构建job信息
            JobDetail job = JobBuilder.newJob(cls).withIdentity(quartz.getJobName(),
                    quartz.getJobGroup())
                    .withDescription(quartz.getDescription()).build();

            //这里传入id作为处理
            job.getJobDataMap().put("requestId", requestId);

            // 触发时间点
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(quartz.getCronExpression());
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger"+quartz.getJobName(), quartz.getJobGroup())
                    .startNow().withSchedule(cronScheduleBuilder).build();
            //交由Scheduler安排触发
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }
    @PostMapping("/list")
    public Result list(QuartzEntity quartz,Integer pageNo,Integer pageSize){
        log.info("任务列表");
        //List<QuartzEntity> list = quartzEntityMapper.selectList(null);
        List<QuartzEntity> list = quartzEntityMapper.listQuartzEntity();
        return Result.ok(list);
    }
    @PostMapping("/trigger")
    public  Result trigger(QuartzEntity quartz, HttpServletResponse response) {
        log.info("触发任务");
        try {
            JobKey key = new JobKey(quartz.getJobName(),quartz.getJobGroup());
            scheduler.triggerJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }
    @PostMapping("/pause")
    public  Result pause(QuartzEntity quartz, HttpServletResponse response) {
        log.info("停止任务");
        try {
            JobKey key = new JobKey(quartz.getJobName(),quartz.getJobGroup());
            scheduler.pauseJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }
    @PostMapping("/resume")
    public  Result resume(QuartzEntity quartz,HttpServletResponse response) {
        log.info("恢复任务");
        try {
            JobKey key = new JobKey(quartz.getJobName(),quartz.getJobGroup());
            scheduler.resumeJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }
    @PostMapping("/remove")
    public  Result remove(QuartzEntity quartz,HttpServletResponse response) {
        log.info("移除任务");
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(quartz.getJobName(), quartz.getJobGroup());
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(quartz.getJobName(), quartz.getJobGroup()));
            System.out.println("removeJob:"+JobKey.jobKey(quartz.getJobName()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }
}
