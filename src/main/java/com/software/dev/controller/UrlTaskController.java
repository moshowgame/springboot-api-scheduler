package com.software.dev.controller;

import com.alibaba.fastjson.JSON;
import com.software.dev.domain.*;
import com.software.dev.job.UrlJob;
import com.software.dev.repository.UrlRequestRepository;
import com.software.dev.repository.UrlResponseRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Task Controller
 * 新封装URL任务控制器
 * @author zhengkai@blog.csdn.net/moshowgame
 * @date 2019/03/10
 */
@RestController
@Slf4j
@RequestMapping("/urlTask")
public class UrlTaskController {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private UrlRequestRepository urlRequestRepository;
    @Autowired
    private UrlResponseRepository urlResponseRepository;

    @PostMapping("/request/info")
    public Result info(@RequestBody PageParam param){
        return Result.ok().put("data",urlRequestRepository.findById(param.getRequestId()).orElse(null));
    }
    @RequestMapping("/request/list")
    public Result requestList(@RequestBody PageParam param){
        log.info("请求列表{}",JSON.toJSONString(param));
        //手动分页
        List<UrlRequest> data= urlRequestRepository.listUrl((param.getPage()-1)*param.getLimit(),param.getLimit(),(StringUtils.isEmpty(param.getSearch()))?null:param.getSearch());
        Long total= urlRequestRepository.count();
//        log.info(JSON.toJSONString(data));
        PageUtils page = new PageUtils(data,total,param.getLimit(),param.getPage());

        return Result.ok().put("page", page);
    }
    @PostMapping("/request/list2")
    public Result requestList2(){
        List<UrlRequest> data= urlRequestRepository.findAllByOrderByRequestIdAsc();
        return Result.ok().put("data", data);
    }
    @PostMapping("/response/list")
    public Result responseList(@RequestBody PageParam param){
        log.info("响应列表{}",JSON.toJSONString(param));
        //JPA分页
        Pageable pageable = PageRequest.of(param.getPage() - 1, param.getLimit());
        org.springframework.data.domain.Page<UrlResponse> page;
        
        if (StringUtils.isNotBlank(param.getRequestId()) && StringUtils.isNotBlank(param.getSearch())) {
            page = urlResponseRepository.findByRequestIdAndResponseTextContainingOrderByResponseTimeDesc(param.getRequestId(), param.getSearch(), pageable);
        } else if (StringUtils.isNotBlank(param.getRequestId())) {
            page = urlResponseRepository.findByRequestIdOrderByResponseTimeDesc(param.getRequestId(), pageable);
        } else if (StringUtils.isNotBlank(param.getSearch())) {
            page = urlResponseRepository.findByResponseTextContainingOrderByResponseTimeDesc(param.getSearch(), pageable);
        } else {
            page = urlResponseRepository.findAllByOrderByResponseTimeDesc(pageable);
        }
        
        PageUtils pageUtils = new PageUtils(page.getContent(), (int) page.getTotalElements(), param.getLimit(), param.getPage());

        return Result.ok().put("page", pageUtils);
    }
    @PostMapping("/request/trigger")
    public  Result trigger(String requestId) {
        log.info("触发任务:"+requestId);
        try {
            JobKey key = new JobKey(requestId,UrlJob.DEFAULT_GROUP);
            scheduler.triggerJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }
    @PostMapping("/request/pause")
    public  Result pause(String requestId) {
        log.info("暂停任务:"+requestId);
        try {
            JobKey key = new JobKey(requestId,UrlJob.DEFAULT_GROUP);
            scheduler.pauseJob(key);
            //修改对应状态
            UrlRequest urlRequest=urlRequestRepository.findById(requestId).orElse(null);
            if(urlRequest!=null) {
                urlRequest.setStatus(UrlRequest.RequestStatus.STOP);
                urlRequestRepository.save(urlRequest);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }

    public  Result remove(String requestId) {
        log.info("移除任务:"+requestId);
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(requestId, UrlJob.DEFAULT_GROUP);
            JobKey jobKey=JobKey.jobKey(requestId, UrlJob.DEFAULT_GROUP);
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(jobKey);
            //修改对应状态
            UrlRequest urlRequest=urlRequestRepository.findById(requestId).orElse(null);
            if(urlRequest!=null) {
                urlRequest.setStatus(UrlRequest.RequestStatus.STOP);
                urlRequestRepository.save(urlRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }
    @PostMapping("/test")
    public Result test(@RequestBody Map<String,Object> map){
        log.info("RequestMap:"+ JSON.toJSONString(map));
        return Result.ok("请求成功");
    }
    @PostMapping("/request/delete")
    public  Result delete(String requestId) {
        log.info("移除任务:"+requestId);
        if(requestId!=null&&urlRequestRepository.existsById(requestId)){
            //先移除任务
            remove(requestId);
            urlRequestRepository.deleteById(requestId);
            return Result.ok("删除成功");
        }else{
            return Result.error("记录不存在");
        }
    }
    @PostMapping("/request/start")
    public Result start(String requestId){
        log.info("启动/恢复任务:"+requestId);
        UrlRequest urlRequest=urlRequestRepository.findById(requestId).orElse(null);
        if(urlRequest==null){
            return Result.error("任务不存在");
        }
        
        try {
            JobKey key = new JobKey(urlRequest.getRequestId(), UrlJob.DEFAULT_GROUP);
            
            if(scheduler.checkExists(key)){
                //任务存在，检查状态
                JobDetail jobDetail = scheduler.getJobDetail(key);
                if(jobDetail != null) {
                    //检查任务是否被暂停，如果是则恢复
                    TriggerKey triggerKey = TriggerKey.triggerKey(urlRequest.getRequestId().toString(), UrlJob.DEFAULT_GROUP);
                    if(scheduler.getTriggerState(triggerKey) == Trigger.TriggerState.PAUSED){
                        log.info("任务已暂停，正在恢复...");
                        scheduler.resumeJob(key);
                    } else {
                        log.info("任务已存在且正常运行");
                    }
                }
            } else {
                //任务不存在，创建新任务
                log.info("任务不存在，正在创建新任务...");
                Class cls = Class.forName(UrlJob.class.getName());
                cls.newInstance();
                
                //构建job信息
                JobDetail job = JobBuilder.newJob(cls)
                        .withIdentity(urlRequest.getRequestId().toString(), UrlJob.DEFAULT_GROUP)
                        .withDescription(urlRequest.getRequestName())
                        .build();

                //这里传入id作为处理
                job.getJobDataMap().put("requestId", requestId);
                
                // 触发时间点
                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(urlRequest.getRequestCron());
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(urlRequest.getRequestId(), UrlJob.DEFAULT_GROUP)
                        .startNow()
                        .withSchedule(cronScheduleBuilder)
                        .build();
                
                //交由Scheduler安排触发
                scheduler.scheduleJob(job, trigger);
                log.info("新任务创建成功");
            }
            
            //修改对应状态
            urlRequest.setStatus(UrlRequest.RequestStatus.START);
            urlRequestRepository.save(urlRequest);
            
        } catch (Exception e) {
            log.error("启动/恢复任务失败", e);
            return Result.error("启动/恢复任务失败: " + e.getMessage());
        }
        
        return Result.ok("任务启动成功");
    }
    @PostMapping("/insertN")
    public  Result insertX(@RequestParam Integer n) {
        for (int i = 0; i < n; i++) {
            UrlRequest urlRequest=new UrlRequest();
            urlRequest.setRequestMethod("POST");
            urlRequestRepository.save(urlRequest);
        }
        return Result.ok();
    }
    @PostMapping("/request/save")
    public  Result save(@RequestBody UrlRequest urlRequest) {
        if(StringUtils.isAnyEmpty(urlRequest.getRequestName(),urlRequest.getRequestCron(),urlRequest.getRequestUrl())){
            return Result.error("请完整填写所有信息");
        }
        if(urlRequest.getStatus()==null){
            urlRequest.setStatus(0);
        }
        if(urlRequest.getRequestMethod()==null){
            urlRequest.setRequestMethod("GET");
        }
        urlRequest.setUpdateTime(new Date());
        urlRequestRepository.save(urlRequest);
        return Result.ok("保存成功");
    }
}
