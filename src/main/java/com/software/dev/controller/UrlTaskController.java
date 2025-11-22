package com.software.dev.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.software.dev.domain.*;
import com.software.dev.job.UrlJob;
import com.software.dev.mapper.UrlRequestMapper;
import com.software.dev.mapper.UrlResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UrlRequestMapper urlRequestMapper;
    @Autowired
    private UrlResponseMapper urlResponseMapper;

    @PostMapping("/request/info")
    public Result info(@RequestBody PageParam param){
        return Result.ok().put("data",urlRequestMapper.selectById(param.getRequestId()));
    }
    @RequestMapping("/request/list")
    public Result requestList(@RequestBody PageParam param){
        log.info("请求列表{}",JSON.toJSONString(param));
        //手动分页
        List<UrlRequest> data= urlRequestMapper.listUrl((param.getPage()-1)*param.getLimit(),param.getLimit(),(StringUtils.isEmpty(param.getSearch()))?null:param.getSearch());
        Long total= urlRequestMapper.selectCount(new QueryWrapper<UrlRequest>());
//        log.info(JSON.toJSONString(data));
        PageUtils page = new PageUtils(data,total,param.getLimit(),param.getPage());

        return Result.ok().put("page", page);
    }
    @PostMapping("/request/list2")
    public Result requestList2(){
        List<UrlRequest> data= urlRequestMapper.selectList(new QueryWrapper<UrlRequest>().select("request_id","request_name").orderByAsc("request_id"));
        return Result.ok().put("data", data);
    }
    @PostMapping("/response/list")
    public Result responseList(@RequestBody PageParam param){
        log.info("响应列表{}",JSON.toJSONString(param));
        //自带分页
        IPage<UrlResponse> iPage= urlResponseMapper.selectPage(new Page<UrlResponse>(param.getPage(),param.getLimit()),
                new QueryWrapper<UrlResponse>().eq(StringUtils.isNotBlank(param.getRequestId()),"request_id",param.getRequestId()).like(StringUtils.isNotBlank(param.getSearch()),"response_text",param.getSearch()).orderByDesc("response_time")
        );
        PageUtils page = new PageUtils(iPage.getRecords(), (int) iPage.getTotal(),param.getLimit(),param.getPage());

        return Result.ok().put("page", page);
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
            UrlRequest urlRequest=urlRequestMapper.selectById(requestId);
            if(urlRequest!=null) {
                urlRequest.setStatus(UrlRequest.RequestStatus.STOP);
                urlRequestMapper.update(urlRequest,
                        new UpdateWrapper<UrlRequest>().eq("request_id", urlRequest.getRequestId())
                );
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok();
    }
    @Deprecated
    @PostMapping("/request/resume")
    public  Result resume(String requestId) {
        log.info("恢复任务:"+ requestId);
        try {
            JobKey key = new JobKey(requestId,UrlJob.DEFAULT_GROUP);
            scheduler.resumeJob(key);
            //修改对应状态
            UrlRequest urlRequest=urlRequestMapper.selectById(requestId);
            if(urlRequest!=null) {
                urlRequest.setStatus(UrlRequest.RequestStatus.START);
                urlRequestMapper.update(urlRequest,
                        new UpdateWrapper<UrlRequest>().eq("request_id", urlRequest.getRequestId())
                );
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
            UrlRequest urlRequest=urlRequestMapper.selectById(requestId);
            if(urlRequest!=null) {
                urlRequest.setStatus(UrlRequest.RequestStatus.STOP);
                urlRequestMapper.update(urlRequest,
                        new UpdateWrapper<UrlRequest>().eq("request_id", urlRequest.getRequestId())
                );
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
        if(requestId!=null&&urlRequestMapper.selectById(requestId)!=null){
            //先移除任务
            remove(requestId);
            urlRequestMapper.deleteById(requestId);
            return Result.ok("删除成功");
        }else{
            return Result.error("记录不存在");
        }
    }
    @PostMapping("/request/start")
    public Result start(String requestId){
        log.info("启动任务:"+requestId);
        UrlRequest urlRequest=urlRequestMapper.selectOne(new QueryWrapper<UrlRequest>().eq("request_id",requestId));
        if(urlRequest!=null){
            try {
                log.info("确认是否有旧任务，删除重新添加");
                JobKey key = new JobKey(urlRequest.getRequestId().toString(), UrlJob.DEFAULT_GROUP);
                if(scheduler.checkExists(key)){
                    //如果存在旧的，删除重新添加
                    scheduler.deleteJob(key);
                }
                Class cls = Class.forName(UrlJob.class.getName()) ;
                cls.newInstance();
                //构建job信息
                JobDetail job = JobBuilder.newJob(cls).withIdentity(urlRequest.getRequestId().toString(),
                        UrlJob.DEFAULT_GROUP)
                        .withDescription(urlRequest.getRequestName()).build();

                //这里传入id作为处理
                job.getJobDataMap().put("requestId", requestId);
                log.info("开始触发新任务");
                // 触发时间点
                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(urlRequest.getRequestCron());
                Trigger trigger = TriggerBuilder.newTrigger().withIdentity(urlRequest.getRequestId().toString(),UrlJob.DEFAULT_GROUP)
                        .startNow().withSchedule(cronScheduleBuilder).build();
                //交由Scheduler安排触发
                scheduler.scheduleJob(job, trigger);
                //修改对应状态
                urlRequest.setStatus(UrlRequest.RequestStatus.START);
                urlRequestMapper.updateById(urlRequest);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
        }
        return Result.ok();
    }
    @PostMapping("/insertN")
    public  Result insertX(@RequestParam Integer n) {
        for (int i = 0; i < n; i++) {
            UrlRequest urlRequest=new UrlRequest();
            urlRequest.setRequestMethod("POST");
            urlRequestMapper.insert(urlRequest);
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
        if(urlRequestMapper.selectById(urlRequest.getRequestId())!=null){
            urlRequestMapper.updateById(urlRequest);
        }else{
            urlRequestMapper.insert(urlRequest);
        }
        return Result.ok("保存成功");
    }
}
