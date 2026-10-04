package com.software.dev.controller;

import com.software.dev.entity.ApiAssertion;
import com.software.dev.entity.ApiTask;
import com.software.dev.service.ApiAssertionService;
import com.software.dev.service.ApiTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 断言配置页面控制器
 * <p>
 * GET /assertions                     —— 完整页面
 * GET /web/assertions/list            —— HTMX 断言列表片段
 * GET /web/assertions/{taskId}/edit   —— HTMX 断言编辑片段
 */
@Controller
public class AssertionPageController {

    @Autowired
    private ApiAssertionService apiAssertionService;

    @Autowired
    private ApiTaskService apiTaskService;

    @GetMapping("/assertions")
    public String page(@RequestParam(required = false) String taskId, Model model) {
        List<ApiTask> tasks = apiTaskService.findAll();
        if ((taskId == null || taskId.isEmpty()) && !tasks.isEmpty()) {
            taskId = tasks.get(0).getId();
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("selectedTaskId", taskId == null ? "" : taskId);
        model.addAttribute("assertions", taskId == null ? List.of() : apiAssertionService.findByTaskId(taskId));
        return "assertions";
    }

    @GetMapping("/web/assertions/list")
    public String list(@RequestParam String taskId, Model model) {
        return renderList(taskId, model, null, false);
    }

    /** 断言编辑片段（每个任务仅一条断言） */
    @GetMapping("/web/assertions/{taskId}/edit")
    public String edit(@PathVariable String taskId, Model model) {
        List<ApiAssertion> assertions = apiAssertionService.findByTaskId(taskId);
        ApiTask task = apiTaskService.findById(taskId);
        model.addAttribute("assertion", assertions.isEmpty() ? null : assertions.get(0));
        model.addAttribute("taskId", taskId);
        model.addAttribute("taskName", task != null ? task.getTaskName() : taskId);
        return "fragments/assertion-form :: modalContent";
    }

    /** 保存断言（每个任务仅一条，存在则更新；类型为空表示清空） */
    @PostMapping("/web/assertions/save")
    public String save(@RequestParam String taskId,
                       @RequestParam(value = "assertionType", required = false) String assertionType,
                       @RequestParam(value = "expectedValue", required = false) String expectedValue,
                       Model model) {
        String message;
        if (assertionType == null || assertionType.isEmpty()) {
            apiAssertionService.deleteByTaskId(taskId);
            message = "已清空断言配置";
        } else {
            ApiAssertion assertion = new ApiAssertion();
            assertion.setTaskId(taskId);
            assertion.setAssertionType(assertionType);
            assertion.setExpectedValue(expectedValue);
            assertion.setSortOrder(0);
            apiAssertionService.saveOrUpdate(assertion);
            message = "断言保存成功";
        }
        return renderList(taskId, model, message, true);
    }

    /** 清空断言 */
    @PostMapping("/web/assertions/{taskId}/clear")
    public String clear(@PathVariable String taskId, Model model) {
        apiAssertionService.deleteByTaskId(taskId);
        return renderList(taskId, model, "已清空断言", false);
    }

    private String renderList(String taskId, Model model, String message, boolean closeModal) {
        model.addAttribute("assertions", apiAssertionService.findByTaskId(taskId));
        model.addAttribute("taskId", taskId);
        model.addAttribute("message", message);
        model.addAttribute("closeModal", closeModal);
        return "fragments/assertion-list :: list";
    }
}
