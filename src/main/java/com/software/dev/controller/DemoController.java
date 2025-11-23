package com.software.dev.controller;

import com.software.dev.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/demo")
@Slf4j
public class DemoController {
    private String defaultToken = "zhengkai.blog.csdn.net";
    @GetMapping("/token")
    public Result getToken(String username,String password){
        log.info("username:{},password:{}",username,password);
        Map<String,String> resultMap = new HashMap<>(2);
        resultMap.put("token",defaultToken);
        resultMap.put("username",username);
        resultMap.put("password",password);
        return Result.ok(resultMap);
    }

    @PostMapping("/request")
    public Result getRequest(String token){
        if(token!=null&&defaultToken.equals(token.trim())){
            return Result.ok("请求成功，TOKEN有效");
        }else{
            return Result.error("请求失败，TOKEN不正确："+token);
        }
    }
}
