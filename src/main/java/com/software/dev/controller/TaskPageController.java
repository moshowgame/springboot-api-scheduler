package com.software.dev.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务管理页面控制器
 * <p>
 * GET /tasks            —— 完整页面
 * GET /web/tasks/list   —— HTMX 任务表格片段
 * 其余 /web/tasks/**    —— HTMX 局部操作，统一返回刷新后的表格片段
 */
@Controller
public class TaskPageController {

    @Autowired
    private ApiTaskService apiTaskService;

    @Autowired
    private AlertService alertService;

    // ==================== 页面 ====================

    @GetMapping("/tasks")
    public String page(Model model) {
        fillTable(model, null, null);
        return "tasks";
    }

    // ==================== HTMX 片段 ====================

    @GetMapping("/web/tasks/list")
    public String list(Model model) {
        fillTable(model, null, null);
        return "fragments/task-list :: table";
    }

    /** 新建任务表单 */
    @GetMapping("/web/tasks/new")
    public String createForm(Model model) {
        ApiTask task = new ApiTask();
        task.setMethod("GET");
        task.setTimeout(30);
        task.setTriggerType("CRON");
        task.setTriggerCondition("ALWAYS");
        fillFormOptions(model, task);
        return "fragments/task-form :: modalContent";
    }

    /** 编辑任务表单 */
    @GetMapping("/web/tasks/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        ApiTask task = apiTaskService.findById(id);
        if (task.getTriggerType() == null) {
            task.setTriggerType("CRON");
        }
        if (task.getTriggerCondition() == null) {
            task.setTriggerCondition("ALWAYS");
        }
        model.addAttribute("task", task);
        fillFormOptions(model, task);
        return "fragments/task-form :: modalContent";
    }

    /** 表单公共选项：触发类型默认值、可选择的下游任务（仅被链式调用类型，排除自身） */
    private void fillFormOptions(Model model, ApiTask task) {
        List<ApiTask> chainTargets = apiTaskService.findAll().stream()
                .filter(t -> t.isChainTriggered() && !t.getId().equals(task.getId()))
                .toList();
        model.addAttribute("chainTargets", chainTargets);
    }

    /** 保存（新建或更新） */
    @PostMapping("/web/tasks/save")
    public String save(@ModelAttribute ApiTask form, Model model) {
        boolean isNew = form.getId() == null || form.getId().isEmpty();
        ApiTask task;
        if (isNew) {
            task = new ApiTask();
        } else {
            task = apiTaskService.findById(form.getId());
            if (task == null) {
                task = new ApiTask();
                isNew = true;
            }
        }
        task.setTaskName(form.getTaskName());
        task.setUrl(form.getUrl());
        task.setMethod(form.getMethod() == null || form.getMethod().isEmpty() ? "GET" : form.getMethod());
        task.setTimeout(form.getTimeout() == null ? 30 : form.getTimeout());
        task.setHeaders(form.getHeaders());
        task.setParameters(form.getParameters());
        task.setCronExpression(form.getCronExpression());
        task.setDescription(form.getDescription());
        // 触发类型互斥：CRON=定时调度 / CHAIN=被链式调用（后者无 Cron 调度）
        String triggerType = "CHAIN".equals(form.getTriggerType()) ? "CHAIN" : "CRON";
        task.setTriggerType(triggerType);
        // 下游触发配置：未选下游任务时条件置为 ALWAYS（列 NOT NULL，保持默认语义）
        String nextTaskId = (form.getNextTaskId() == null || form.getNextTaskId().isEmpty()
                || form.getNextTaskId().equals(task.getId())) ? null : form.getNextTaskId();
        task.setNextTaskId(nextTaskId);
        task.setTriggerCondition(nextTaskId == null ? "ALWAYS"
                : switch (form.getTriggerCondition() == null ? "" : form.getTriggerCondition()) {
                    case "ASSERTION_PASS", "ASSERTION_FAIL" -> form.getTriggerCondition();
                    default -> "ALWAYS";
                });

        if (isNew) {
            apiTaskService.save(task);
        } else {
            apiTaskService.update(task);
        }
        return renderTable(model, isNew ? "任务创建成功" : "任务更新成功");
    }

    @PostMapping("/web/tasks/{id}/start")
    public String start(@PathVariable String id, Model model) {
        apiTaskService.startTask(id);
        return renderTable(model, "任务已启动");
    }

    @PostMapping("/web/tasks/{id}/pause")
    public String pause(@PathVariable String id, Model model) {
        apiTaskService.pauseTask(id);
        return renderTable(model, "任务已暂停");
    }

    @PostMapping("/web/tasks/{id}/execute")
    public String execute(@PathVariable String id, Model model) {
        apiTaskService.executeTask(id);
        return renderTable(model, "已触发一次执行");
    }

    @PostMapping("/web/tasks/{id}/delete")
    public String delete(@PathVariable String id, Model model) {
        apiTaskService.deleteById(id);
        return renderTable(model, "任务已删除");
    }

    @PostMapping("/web/tasks/{id}/alert/{enabled}")
    public String toggleAlert(@PathVariable String id, @PathVariable boolean enabled, Model model) {
        alertService.enableTaskAlert(id, enabled);
        return renderTable(model, enabled ? "已启用警报" : "已禁用警报");
    }

    /** 批量操作 */
    @PostMapping("/web/tasks/batch")
    public String batch(@RequestParam(value = "ids", required = false) List<String> ids,
                        @RequestParam String action,
                        Model model) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                switch (action) {
                    case "start" -> apiTaskService.startTask(id);
                    case "pause" -> apiTaskService.pauseTask(id);
                    case "enableAlert" -> alertService.enableTaskAlert(id, true);
                    case "disableAlert" -> alertService.enableTaskAlert(id, false);
                    case "delete" -> apiTaskService.deleteById(id);
                    default -> { /* 忽略未知操作 */ }
                }
            }
        }
        String message = (ids == null || ids.isEmpty())
                ? "请先勾选任务"
                : "批量操作完成，共处理 " + ids.size() + " 个任务";
        return renderTable(model, message);
    }

    private String renderTable(Model model, String message) {
        return fillTable(model, message, true);
    }

    /** 组装任务表格片段数据：任务列表 + 下游任务名映射（用于链式触发展示） */
    private String fillTable(Model model, String message, Boolean closeModal) {
        List<ApiTask> tasks = apiTaskService.findAll();
        Map<String, String> taskNameById = tasks.stream()
                .collect(Collectors.toMap(ApiTask::getId, t -> t.getTaskName() == null ? t.getId() : t.getTaskName(), (a, b) -> a));
        model.addAttribute("tasks", tasks);
        model.addAttribute("taskNameById", taskNameById);
        if (message != null) {
            model.addAttribute("message", message);
            model.addAttribute("closeModal", closeModal);
        }
        return "fragments/task-list :: table";
    }
}
