package com.software.dev.controller;

import com.software.dev.entity.ApiTask;
import com.software.dev.entity.Statistics;
import com.software.dev.service.ApiTaskService;
import com.software.dev.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 数据统计页面控制器
 * <p>
 * GET /dashboard            —— 完整页面
 * GET /web/dashboard/stats  —— HTMX 统计片段（按天数 / 任务筛选）
 */
@Controller
public class DashboardPageController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ApiTaskService apiTaskService;

    @GetMapping("/dashboard")
    public String page(@RequestParam(defaultValue = "7") Integer days,
                       @RequestParam(required = false) String taskId,
                       Model model) {
        fill(model, days, taskId);
        return "dashboard";
    }

    @GetMapping("/web/dashboard/stats")
    public String stats(@RequestParam(defaultValue = "7") Integer days,
                        @RequestParam(required = false) String taskId,
                        Model model) {
        fill(model, days, taskId);
        return "fragments/dashboard-stats :: stats";
    }

    private void fill(Model model, Integer days, String taskId) {
        if (days == null || days < 1) {
            days = 7;
        }
        String tid = (taskId != null && !taskId.isEmpty()) ? taskId : null;
        Statistics statistics = statisticsService.getStatistics(days, tid);
        List<ApiTask> tasks = apiTaskService.findAll();

        model.addAttribute("stats", statistics);
        model.addAttribute("tasks", tasks);
        model.addAttribute("days", days);
        model.addAttribute("filterTaskId", taskId == null ? "" : taskId);
    }
}
