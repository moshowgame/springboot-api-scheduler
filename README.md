# quartz-api-scheduler
![image](https://img.shields.io/badge/Quartz-%E2%98%85%E2%98%85%E2%98%85%E2%98%85%E2%98%85-brightgreen.svg)
![image](https://img.shields.io/badge/springboot2-%E2%98%85%E2%98%85%E2%98%85%E2%98%85%E2%98%85-brightgreen.svg)
[![Build Status](https://api.travis-ci.org/moshowgame/quartz-api-scheduler.svg?branch=master)](https://api.travis-ci.org/moshowgame/quartz-api-scheduler.svg)

## 项目介绍

`Quartz-API-Scheduler` 是一个基于 `Quartz` 2.5 + `SpringBoot` 3 的轻量级API调度平台。

➡️ 开箱即用：默认支持单机部署，满足日常任务调度需求。

➡️ 灵活扩展：可平滑升级为集群部署，适应更大规模的分布式场景。

➡️ 简化调度：将复杂的任务调度抽象为 URL 调用，让定时任务配置与管理更加直观、便捷。

🔥它的设计理念是：“让任务调度像调用接口一样简单”。无论是定时触发业务逻辑，还是统一管理分布式任务，Quartz-API-Scheduler 都能提供高效、易用的解决方案。

🏆Powered by [Moshow郑锴](https://zhengkai.blog.csdn.net/) 

## 核心特性

### URL-PLUS
基于 Quartz 的二次封装，URL Request 是二次封装的核心，包含：
- 基本 UrlRequest（method、url、cron、name）
- UrlResponse（响应日志）

### SchedulerPlus 增强功能
- RequestToken（令牌设置 Header Token、Form Token、Url Token）
- RequestParam（请求参数，追加 FormData 或 JSON 等参数和报文，设置 GET/POST 属性）
- ResponseAssumption（响应断言，根据返回结果判断是否成功，例如设置 keyword="code":"00",state=1，匹配到返回对象包含 keyword 则代表成功，否则失败）

## Token 添加方式
- Header Token（待实现）
- Form Token（已完成）
- Url Token（已完成）

## 请求参数支持
- GET URL（已完成）
- POST FROM（已完成）
- POST JSON（已完成）

## 响应断言
- 页面功能已完成，功能待完善

## T_TASK_TRIGGERS 表 TRIGGER_STATE 状态字段
- WAITING: 等待执行中
- PAUSED: 任务暂停
- ACQUIRED: 正在执行中
- COMPLETE: 执行完成
- BLOCKED: 任务阻塞 
- ERROR: 任务错误

![Quartz 状态图](./quartz-status.png)

## 快速开始

### 测试接口
- 测试 token 页面: http://localhost:17777/quartz/demo/token?username=111&password=222
- 前端页面 (请求列表): http://localhost:17777/quartz/index#request/list

![界面截图1](./image1.png)
![界面截图2](./image2.png)
![界面截图3](./image3.png)
![界面截图4](./image4.png)

## 更新日志

| 日期         | 内容                                                                                                 |
|------------|----------------------------------------------------------------------------------------------------|
| 2025-11-23 | Quartz2.5+SpringBoot3.5版本升级<br>修改数据库为PostgreSQL数据库<bt>数据库连接成使用默认hikari<br>Web容器更换为Jetty<br>其他依赖库优化 |
| 2025-11-22 | 项目正式更名为 quartz-api-scheduler，并计划一波大更新!                                                             |
| 2022-11-26 | 回滚页面到旧版本并进行优化                                                                                      |
| 2022-03-06 | 实现响应推断处理逻辑，优化 UI 显示逻辑                                                                              |
| 2022-02-25 | 修复 Token 页面，新增 Assumption 内容                                                                       |
| 2022-02-20 | 基于墨菲安全进行安全扫描，更新相关依赖                                                                                |
| 2021-03-28 | 优化以及修复请求、响应，UI 优化，修复执行问题，新增登录功能                                                                    |
| 2021-03-27 | 重启项目 2.0 版本，UI 改版，UrlRequest 优化                                                                    |
| 2019-04-28 | UrlPlus 之 Url 追加 Token 参数功能，token 配置功能，优化 gitignore                                                |
| 2019-04-11 | 优化管理页面，修复一些细节问题，新增日志查看功能，新增 travis                                                                 |
| 2019-04-07 | 优化核心模块核心状态的封装，包含状态变更简化，管理界面优化                                                                      |
| 2019-04-03 | UrlJob 里面的 Log 信息优化                                                                                |
| 2019-03-18 | 一些简单的页面                                                                                            |
| 2019-03-15 | 分离新旧接口，新封装的在 UrlTaskController 里面，quartz 原生的在 JobController 里面                                     |