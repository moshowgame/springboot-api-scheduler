package com.software.dev.controller;

import com.software.dev.entity.ApiResponse;
import com.software.dev.entity.ApiTask;
import com.software.dev.service.ApiResponseService;
import com.software.dev.service.ApiTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行日志页面控制器
 * <p>
 * GET /logs              —— 完整页面
 * GET /web/logs/list     —— HTMX 日志表格片段（分页 + 筛选）
 * GET /web/logs/{id}/detail —— HTMX 日志详情片段
 */
@Controller
public class LogPageController {

    @Autowired
    private ApiResponseService apiResponseService;

    @Autowired
    private ApiTaskService apiTaskService;

    @GetMapping("/logs")
    public String page(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String taskId,
                       @RequestParam(required = false) String startTime,
                       @RequestParam(required = false) String endTime,
                       Model model) {
        fill(model, page, size, taskId, startTime, endTime);
        return "logs";
    }

    @GetMapping("/web/logs/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String taskId,
                       @RequestParam(required = false) String startTime,
                       @RequestParam(required = false) String endTime,
                       Model model) {
        fill(model, page, size, taskId, startTime, endTime);
        return "fragments/log-list :: table";
    }

    @GetMapping("/web/logs/{id}/detail")
    public String detail(@PathVariable String id, Model model) {
        ApiResponse resp = apiResponseService.findById(id);
        model.addAttribute("resp", resp);
        if (resp != null) {
            ApiTask task = apiTaskService.findById(resp.getTaskId());
            model.addAttribute("taskName", task != null ? task.getTaskName() : resp.getTaskId());
        }
        return "fragments/log-detail :: modalContent";
    }

    private void fill(Model model, int page, int size, String taskId, String startTime, String endTime) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 200) {
            size = 20;
        }
        String tid = (taskId != null && !taskId.isEmpty()) ? taskId : null;
        String st = normalizeStart(startTime);
        String et = normalizeEnd(endTime);

        int total = apiResponseService.countByConditions(tid, st, et);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) size));
        if (page > totalPages) {
            page = totalPages;
        }
        List<ApiResponse> logs = apiResponseService.findByPageWithConditions(page, size, tid, st, et);

        model.addAttribute("logs", logs);
        model.addAttribute("tasks", apiTaskService.findAll());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageLinks", buildPageLinks(page, totalPages, size, taskId, startTime, endTime));
        model.addAttribute("filterTaskId", taskId == null ? "" : taskId);
        model.addAttribute("filterStartTime", startTime == null ? "" : startTime);
        model.addAttribute("filterEndTime", endTime == null ? "" : endTime);
    }

    /** 补全日期为完整时间戳，便于 PostgreSQL 比较 */
    private String normalizeStart(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String v = value.replace('T', ' ').trim();
        return v.length() == 10 ? v + " 00:00:00" : v;
    }

    private String normalizeEnd(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String v = value.replace('T', ' ').trim();
        return v.length() == 10 ? v + " 23:59:59" : v;
    }

    private List<Integer> pageWindow(int page, int totalPages) {
        List<Integer> pages = new ArrayList<>();
        int start = Math.max(1, page - 2);
        int end = Math.min(totalPages, start + 4);
        start = Math.max(1, end - 4);
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }

    /** 预生成分页链接（URL 在服务端拼装，模板无需处理复杂表达式） */
    private List<Map<String, Object>> buildPageLinks(int page, int totalPages, int size,
                                                     String taskId, String startTime, String endTime) {
        List<Map<String, Object>> links = new ArrayList<>();
        for (int p : pageWindow(page, totalPages)) {
            Map<String, Object> link = new HashMap<>();
            link.put("num", p);
            link.put("active", p == page);
            link.put("url", pageUrl(p, size, taskId, startTime, endTime));
            links.add(link);
        }
        return links;
    }

    private String pageUrl(int page, int size, String taskId, String startTime, String endTime) {
        StringBuilder sb = new StringBuilder("/web/logs/list?page=").append(page).append("&size=").append(size);
        appendParam(sb, "taskId", taskId);
        appendParam(sb, "startTime", startTime);
        appendParam(sb, "endTime", endTime);
        return sb.toString();
    }

    private void appendParam(StringBuilder sb, String name, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append('&').append(name).append('=')
              .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }
}
