package com.software.dev.controller;

import com.software.dev.entity.AlertConfig;
import com.software.dev.entity.AlertRecord;
import com.software.dev.entity.ApiTask;
import com.software.dev.service.AlertService;
import com.software.dev.service.ApiTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 警报记录页面控制器
 * <p>
 * GET /alerts                —— 完整页面
 * GET /web/alerts/list       —— HTMX 警报记录片段（分页 + 任务名筛选）
 * GET /web/alerts/{taskId}/config —— 警报配置表单片段
 */
@Controller
public class AlertPageController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private ApiTaskService apiTaskService;

    @GetMapping("/alerts")
    public String page(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String taskName,
                       Model model) {
        fill(model, page, size, taskName);
        return "alerts";
    }

    @GetMapping("/web/alerts/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String taskName,
                       Model model) {
        fill(model, page, size, taskName);
        return "fragments/alert-list :: table";
    }

    /** 打开某任务的警报配置表单 */
    @GetMapping("/web/alerts/config-form")
    public String configForm(@RequestParam String taskId, Model model) {
        AlertConfig config = alertService.getAlertConfigByTaskId(taskId);
        if (config == null) {
            config = new AlertConfig();
            config.setTaskId(taskId);
            config.setFailureRateThreshold(50);
            config.setCheckInterval(60);
            config.setHttpMethod("POST");
            config.setEnabled(true);
        }
        ApiTask task = apiTaskService.findById(taskId);
        model.addAttribute("config", config);
        model.addAttribute("task", task);
        return "fragments/alert-config :: modalContent";
    }

    /** 保存警报配置 */
    @PostMapping("/web/alerts/config")
    public String saveConfig(@ModelAttribute AlertConfig config,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) String taskName,
                             Model model) {
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        boolean ok = alertService.saveAlertConfig(config);
        fill(model, page, size, taskName);
        model.addAttribute("message", ok ? "警报配置保存成功" : "警报配置保存失败");
        model.addAttribute("closeModal", true);
        return "fragments/alert-list :: table";
    }

    @PostMapping("/web/alerts/{taskId}/enable")
    public String enable(@PathVariable String taskId,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(required = false) String taskName,
                         Model model) {
        alertService.enableTaskAlert(taskId, true);
        fill(model, page, size, taskName);
        model.addAttribute("message", "已启用警报");
        return "fragments/alert-list :: table";
    }

    @PostMapping("/web/alerts/{taskId}/disable")
    public String disable(@PathVariable String taskId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String taskName,
                          Model model) {
        alertService.enableTaskAlert(taskId, false);
        fill(model, page, size, taskName);
        model.addAttribute("message", "已禁用警报");
        return "fragments/alert-list :: table";
    }

    private void fill(Model model, int page, int size, String taskName) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 200) {
            size = 20;
        }
        String name = (taskName != null && !taskName.isEmpty()) ? taskName : null;

        int total = alertService.countAlertRecords(name);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) size));
        if (page > totalPages) {
            page = totalPages;
        }
        List<AlertRecord> records = alertService.getAlertRecordsByPage(page, size, name);

        model.addAttribute("records", records);
        model.addAttribute("tasks", apiTaskService.findAll());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageLinks", buildPageLinks(page, totalPages, size, taskName));
        model.addAttribute("filterTaskName", taskName == null ? "" : taskName);
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

    /** 预生成分页链接 */
    private List<Map<String, Object>> buildPageLinks(int page, int totalPages, int size, String taskName) {
        List<Map<String, Object>> links = new ArrayList<>();
        for (int p : pageWindow(page, totalPages)) {
            Map<String, Object> link = new HashMap<>();
            link.put("num", p);
            link.put("active", p == page);
            StringBuilder url = new StringBuilder("/web/alerts/list?page=").append(p).append("&size=").append(size);
            if (taskName != null && !taskName.isEmpty()) {
                url.append("&taskName=").append(URLEncoder.encode(taskName, StandardCharsets.UTF_8));
            }
            link.put("url", url.toString());
            links.add(link);
        }
        return links;
    }
}
