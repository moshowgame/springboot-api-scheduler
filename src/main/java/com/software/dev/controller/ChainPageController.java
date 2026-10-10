package com.software.dev.controller;

import com.software.dev.entity.ApiResponse;
import com.software.dev.entity.ApiTask;
import com.software.dev.service.ApiResponseService;
import com.software.dev.service.ApiTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务链页面控制器（简易任务编排可视化）
 * <p>
 * GET /chains —— 任务链总览：以「定时调度」或未被上游引用的任务为链头，
 * 沿 next_task_id 指针展开整条链路，并展示每节点的最近执行结果；
 * 成环时截断并标记，未被任何上游引用的「被链式调用」任务单独列为游离节点。
 */
@Controller
public class ChainPageController {

    @Autowired
    private ApiTaskService apiTaskService;

    @Autowired
    private ApiResponseService apiResponseService;

    @GetMapping("/chains")
    public String page(Model model) {
        List<ApiTask> tasks = apiTaskService.findAll();
        Map<String, ApiTask> taskById = new LinkedHashMap<>();
        for (ApiTask t : tasks) {
            taskById.put(t.getId(), t);
        }

        // 下游指针: taskId -> nextTaskId
        Map<String, String> nextMap = new LinkedHashMap<>();
        Set<String> referenced = new HashSet<>();
        for (ApiTask t : tasks) {
            String next = t.getNextTaskId();
            if (next != null && !next.isEmpty() && taskById.containsKey(next)) {
                nextMap.put(t.getId(), next);
                referenced.add(next);
            }
        }

        // 链头：有下游且自身不被任何任务引用
        List<ApiTask> heads = new ArrayList<>();
        for (ApiTask t : tasks) {
            if (nextMap.containsKey(t.getId()) && !referenced.contains(t.getId())) {
                heads.add(t);
            }
        }

        List<Map<String, Object>> chains = new ArrayList<>();
        for (ApiTask head : heads) {
            chains.add(buildChain(head, nextMap, taskById));
        }

        // 游离的链式任务：类型为 CHAIN，但既没有上游引用，也没有配置下游
        List<Map<String, Object>> orphans = new ArrayList<>();
        for (ApiTask t : tasks) {
            if (t.isChainTriggered() && !referenced.contains(t.getId()) && !nextMap.containsKey(t.getId())) {
                Map<String, Object> node = new HashMap<>();
                node.put("task", t);
                node.put("last", latestResponse(t.getId()));
                orphans.add(node);
            }
        }

        model.addAttribute("chains", chains);
        model.addAttribute("orphans", orphans);
        return "chains";
    }

    /** 从链头沿指针展开链路，成环时截断并标记 */
    private Map<String, Object> buildChain(ApiTask head, Map<String, String> nextMap, Map<String, ApiTask> taskById) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        boolean cycle = false;

        String condition = null; // 到达当前节点的边条件（链头为 null）
        ApiTask current = head;
        while (current != null) {
            if (visited.contains(current.getId())) {
                cycle = true;
                break;
            }
            visited.add(current.getId());
            Map<String, Object> node = new HashMap<>();
            node.put("task", current);
            node.put("condition", condition);
            node.put("last", latestResponse(current.getId()));
            nodes.add(node);

            String nextId = nextMap.get(current.getId());
            condition = nextId != null ? nextMapCondition(taskById.get(current.getId())) : null;
            current = nextId != null ? taskById.get(nextId) : null;
        }

        Map<String, Object> chain = new HashMap<>();
        chain.put("nodes", nodes);
        chain.put("cycle", cycle);
        return chain;
    }

    private String nextMapCondition(ApiTask task) {
        if (task == null || task.getTriggerCondition() == null) {
            return "ALWAYS";
        }
        return task.getTriggerCondition();
    }

    private ApiResponse latestResponse(String taskId) {
        List<ApiResponse> list = apiResponseService.findByPageWithConditions(1, 1, taskId, null, null);
        return list.isEmpty() ? null : list.get(0);
    }
}
