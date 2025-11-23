package com.software.dev.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.software.dev.domain.UrlRequestToken;
import com.software.dev.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Service
@Slf4j
/**
 * UrlPlus一些封装方法
 * @author zhengkai.blog.csdn.net
 */
public class UrlPlusService  implements Serializable {

    /**
     * 获取Token
     * @author zhengkai.blog.csdn.net
     */
    public String getToken(UrlRequestToken urlRequestToken) throws IOException {
        String  responseMsg="";
        //GET请求
        if(UrlRequestToken.Method.GET.equals(urlRequestToken.getMethod())){
            responseMsg= HttpUtil.get(urlRequestToken.getTokenUrl(),null,null);
        }else{
            //POST请求
            //处理FORM类型param参数
            if(UrlRequestToken.ParamType.FORM.equals(urlRequestToken.getParamType())){
                JSONObject paramObject=JSONObject.parseObject(urlRequestToken.getParam());
                //对于form类型，把JSON拿出来转换为map，再请求
                Map<String,String> paramMap=(Map)paramObject;
                log.info("paramMap:"+paramMap);
                responseMsg=HttpUtil.post(urlRequestToken.getTokenUrl(),null,paramMap);
            }else{
                log.info("paramBody:"+urlRequestToken.getParam());
                //处理JSON类型param参数，直接放body请求
                responseMsg=HttpUtil.post(urlRequestToken.getTokenUrl(),null,urlRequestToken.getParam());
            }
        }
        JSONObject tokenObject = JSONObject.parseObject(responseMsg);
        log.info("response:{}",responseMsg);
        String token=null;
        //未定义获取Token表达
        if(StringUtils.isBlank(urlRequestToken.getTokenExpression())){
            for(String key:tokenObject.keySet()){
                //如果包含token，则直接找到，返回即可
                if(key.equalsIgnoreCase("token")){
                    token=tokenObject.getString(key);
                    log.info("token:"+token);
                }
            }
        }else{
            JSONObject tokenObjectX=tokenObject;
            //已定义，直接分割
            String[] strings=urlRequestToken.getTokenExpression().split("\\.");
            log.info("tokenObject:"+ tokenObject);
            log.info("spiltStrings:"+ JSON.toJSONString(strings));
            for(int i=0;i<strings.length;i++){
                if((i+1)==strings.length){
                    token=tokenObjectX.getString(strings[i]);
                }else{
                    tokenObjectX=tokenObjectX.getJSONObject(strings[i]);
                }
                //tokenObject.get
            }
            log.info("最终token="+token);
        }
        return token;
    }
}