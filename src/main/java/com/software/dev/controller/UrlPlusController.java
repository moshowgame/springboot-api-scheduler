package com.software.dev.controller;

import com.alibaba.fastjson.JSON;
import com.software.dev.domain.*;
import com.software.dev.repository.UrlRequestTokenRepository;
import com.software.dev.repository.UrlResponseAssumptionRepository;
import com.software.dev.service.UrlPlusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UrlPlus Controller
 * 封装增强处理器
 * @author zhengkai@blog.csdn.net/moshowgame
 * @date 2019/04/28
 */
@RestController
@Slf4j
@RequestMapping("/urlPlus")
public class UrlPlusController {

    @Autowired
    private UrlRequestTokenRepository urlRequestTokenRepository;
    @Autowired
    private UrlResponseAssumptionRepository urlResponseAssumptionRepository;

    @Autowired
    private UrlPlusService urlPlusService;

    @PostMapping("/token/list")
    public Result tokenList(@RequestBody PageParam param){
        log.info("响应列表{}",JSON.toJSONString(param));
        //JPA分页
        Pageable pageable = PageRequest.of(param.getPage() - 1, param.getLimit());
        org.springframework.data.domain.Page<UrlRequestToken> page;
        if (StringUtils.isNotBlank(param.getSearch())) {
            page = urlRequestTokenRepository.findByStatusAndTokenUrlContainingOrderByRequestIdDesc(1, param.getSearch(), pageable);
        } else {
            page = urlRequestTokenRepository.findByStatusOrderByRequestIdDesc(1, pageable);
        }
        PageUtils pageUtils = new PageUtils(page.getContent(), (int) page.getTotalElements(), param.getLimit(), param.getPage());

        return Result.ok().put("page", pageUtils);
    }
    @PostMapping("/token/info")
    public Result tokenQuery(@RequestBody PageParam pageParam){
        UrlRequestToken urlRequestToken=urlRequestTokenRepository.findById(pageParam.getRequestId()).orElse(null);
        if(urlRequestToken==null){
            return Result.error("找不到对应的token，新建一个吧");
        }else{
            return Result.ok(urlRequestToken);
        }
    }
    @PostMapping("/assumption/info")
    public Result assumptionQuery(@RequestBody PageParam pageParam){
        UrlResponseAssumption urlResponseAssumption=urlResponseAssumptionRepository.findById(pageParam.getRequestId()).orElse(null);
        if(urlResponseAssumption==null){
            return Result.error("找不到对应的assumption，新建一个吧");
        }else{
            return Result.ok(urlResponseAssumption);
        }
    }

    @PostMapping("/token/test")
    public Result tokenTest(@RequestBody PageParam pageParam){
        UrlRequestToken urlRequestToken=urlRequestTokenRepository.findById(pageParam.getRequestId()).orElse(null);
        if(urlRequestToken==null){
            return Result.error("找不到对应的token");
        }else{
            try {
                return Result.ok(urlPlusService.getToken(urlRequestToken));
            }catch (Exception e){
                return Result.error("请求失败:"+e.getMessage());
            }
        }
    }
    /**
     * 新增或编辑
     */
    @PostMapping("/token/save")
    public Result tokenSave(@RequestBody UrlRequestToken urlRequestToken){
        log.info("tokenSaveOrUpdate:"+urlRequestToken.getRequestId());
        if(StringUtils.isAnyEmpty(urlRequestToken.getRequestId(),urlRequestToken.getParam(),urlRequestToken.getTokenUrl())){
            return Result.error("请完善信息后提交");
        }
        if(urlRequestTokenRepository.existsById(urlRequestToken.getRequestId())){
            urlRequestTokenRepository.save(urlRequestToken);
        }else{
            urlRequestTokenRepository.save(urlRequestToken);
        }
        return Result.ok("TOKEN保存成功");
    }
    @PostMapping("/assumption/save")
    public Result assumptionSave(@RequestBody UrlResponseAssumption urlResponseAssumption){
        log.info("assumptionSaveOrUpdate:"+urlResponseAssumption.getRequestId());
        if(StringUtils.isAnyEmpty(urlResponseAssumption.getRequestId(),urlResponseAssumption.getKeyFirst(),urlResponseAssumption.getValueFirst(),urlResponseAssumption.getValueElse())){
            return Result.error("请完善信息后提交");
        }
        if(urlResponseAssumptionRepository.existsById(urlResponseAssumption.getRequestId())){
            urlResponseAssumptionRepository.save(urlResponseAssumption);
        }else{
            urlResponseAssumptionRepository.save(urlResponseAssumption);
        }
        return Result.ok("ASSUMPTION保存成功");
    }
}
