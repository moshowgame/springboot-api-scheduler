package com.software.dev.job;

import com.software.dev.domain.UrlRequest;
import com.software.dev.domain.UrlRequestToken;
import com.software.dev.domain.UrlResponse;
import com.software.dev.domain.UrlResponseAssumption;
import com.software.dev.repository.UrlRequestRepository;
import com.software.dev.repository.UrlRequestTokenRepository;
import com.software.dev.repository.UrlResponseRepository;
import com.software.dev.repository.UrlResponseAssumptionRepository;
import com.software.dev.service.UrlPlusService;
import com.software.dev.util.DateUtil;
import com.software.dev.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * UrlPlugin之UrlJob
 * @author  zhengkai@blog.csdn.net/moshowgame
 * * @DisallowConcurrentExecution 保证上一个任务执行完后，再去执行下一个任务
 */
@DisallowConcurrentExecution
@Slf4j
@Component
public class UrlJob  implements Job, Serializable {

    private static final long serialVersionUID = 1L;

    public static String DEFAULT_GROUP="DEFAULT";

    @Autowired
    private UrlResponseRepository urlResponseRepository;
    @Autowired
    private UrlRequestRepository urlRequestRepository;
    @Autowired
    private UrlPlusService urlPlusService;
    @Autowired
    private UrlRequestTokenRepository urlRequestTokenRepository;
    @Autowired
    private UrlResponseAssumptionRepository urlResponseAssumptionRepository;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.info("EasyUrlTask is executing the job:"+ DateUtil.now());
        //取得job详情
        JobDetail jobDetail = context.getJobDetail();
        // 取得job名称
        //String jobName = jobDetail.getClass().getName();
        //log.info("Job Name:" + jobName);
        //取得job的类 ，由于UrlPlugin是同个class不同参数，所以这里都是同个class
        //log.info("Job Class:" + jobDetail.getJobClass());
        //取得job开始时间
        //log.info("start/fire time: " + DateUtil.formatHttpDate(context.getFireTime()));
        //取得job下次触发时间
        log.info("Next fire time: {}" , DateUtil.formatHttpDate(context.getNextFireTime()));

        JobDataMap dataMap =  jobDetail.getJobDataMap();
        String urlId=dataMap.getString("requestId");
        log.info("URL REQUEST ID: {}", urlId);

        String responseMsg=null;
        //Url Request
        if(!StringUtils.isBlank(urlId)){
            UrlRequest urlRequest=urlRequestRepository.findById(urlId).orElse(null);
            UrlRequestToken urlRequestToken=urlRequestTokenRepository.findById(urlId).orElse(null);
            //成功找到请求id
            if(urlRequest!=null){
                String requestUrl=urlRequest.getRequestUrl();
                log.info("URL REQUEST NAME: {} ",urlRequest.getRequestName());
                //如果存在token，
                if(urlRequestToken!=null&&urlRequestToken.getStatus()==1){
                    String tokenStr= null;
                    try {
                        tokenStr = urlPlusService.getToken(urlRequestToken);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    log.info("URL REQUEST TOKEN: {}",tokenStr);
                    //处理URL追加类型（优先）
                    if(UrlRequestToken.AppendType.URL.equals(urlRequestToken.getAppendType())){
                        if(requestUrl.endsWith(urlRequestToken.getAppendName()+"=")){
                            //如果预留的尾部一致，例如accessToken=,然后AppendName也叫accessToken，属于匹配追加
                            requestUrl+=tokenStr;
                        }else if(requestUrl.contains("?")){
                            //如果包含问号，则无论有多少个&，继续追加&即可
                            requestUrl=requestUrl+"&"+urlRequestToken.getAppendName()+"="+tokenStr;
                        }else{
                            //如果未包含问号，则需要追加?xxx=xxx
                            requestUrl=requestUrl+"?"+urlRequestToken.getAppendName()+"="+tokenStr;
                        }
                    }else{
                        //处理FORM追加类型

                    }
                }else{
                    log.info("不存在或未启用TOKEN");
                }
                //处理响应
                UrlResponse urlResponse=new UrlResponse();
                urlResponse.setRequestId(urlId);
                urlResponse.setRequestTime(new Date());
                log.info(urlRequest.getRequestMethod()+":" + requestUrl);
                //if()
                //请求GET/POST
                try{
                    if(UrlRequest.RequestMethod.GET.equals(urlRequest.getRequestMethod())){
                        responseMsg = HttpUtil.get(requestUrl,null,null);
                    }else if (UrlRequest.RequestMethod.POST.equals(urlRequest.getRequestMethod())){
                        responseMsg = HttpUtil.post(requestUrl,null,"");
                    }
                    urlResponse.setStatus(1);
                    urlResponse.setResponseText(responseMsg);
                    //URL PLUS之推断逻辑 by Moshow 2022-03-06
                    UrlResponseAssumption urlResponseAssumption = urlResponseAssumptionRepository.findById(urlId).orElse(null);
                    if(urlResponseAssumption!=null){
                        if(!StringUtils.isAllEmpty(responseMsg,urlResponseAssumption.getKeyFirst())&&responseMsg.contains(urlResponseAssumption.getKeyFirst())){
                            urlResponse.setAssumptionResult(urlResponseAssumption.getValueFirst());
                        }else if(!StringUtils.isAllEmpty(responseMsg,urlResponseAssumption.getKeySecond())&&responseMsg.contains(urlResponseAssumption.getKeySecond())){
                            urlResponse.setAssumptionResult(urlResponseAssumption.getValueSecond());
                        }else if(!StringUtils.isAllEmpty(responseMsg,urlResponseAssumption.getKeyThird())&&responseMsg.contains(urlResponseAssumption.getKeyThird())){
                            urlResponse.setAssumptionResult(urlResponseAssumption.getValueThird());
                        }else{
                            urlResponse.setAssumptionResult(urlResponseAssumption.getValueElse());
                        }
                    }
                }catch(Exception e){
                    //优化异常提示
                    urlResponse.setStatus(9);
                    urlResponse.setResponseText(e.getMessage());
                }
                log.info("RESPONSE TEXT:"+responseMsg);
                urlResponse.setResponseId(DateUtil.generateTimestampId());
                urlResponse.setResponseTime(new Date());
                urlResponseRepository.save(urlResponse);
            }
        }


    }
}
