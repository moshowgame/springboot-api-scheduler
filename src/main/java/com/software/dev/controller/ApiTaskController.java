package com.software.dev.controller;

import com.software.dev.entity.ApiTask;
import com.software.dev.service.ApiTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class ApiTaskController {

    @Autowired
    private ApiTaskService apiTaskService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTasks() {
        List<ApiTask> tasks = apiTaskService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", tasks);
        result.put("message", "Success");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTaskById(@PathVariable String id) {
        ApiTask task = apiTaskService.findById(id);
        Map<String, Object> result = new HashMap<>();
        if (task != null) {
            result.put("code", 200);
            result.put("data", task);
            result.put("message", "Success");
        } else {
            result.put("code", 404);
            result.put("message", "Task not found");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody ApiTask apiTask) {
        int result = apiTaskService.save(apiTask);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("data", apiTask);
            response.put("message", "Task created successfully");
        } else {
            response.put("code", 500);
            response.put("message", "Failed to create task");
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable String id, @RequestBody ApiTask apiTask) {
        apiTask.setId(id);
        int result = apiTaskService.update(apiTask);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("data", apiTask);
            response.put("message", "Task updated successfully");
        } else {
            response.put("code", 500);
            response.put("message", "Failed to update task");
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable String id) {
        int result = apiTaskService.deleteById(id);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("message", "Task deleted successfully");
        } else {
            response.put("code", 500);
            response.put("message", "Failed to delete task");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startTask(@PathVariable String id) {
        apiTaskService.startTask(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Task started successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseTask(@PathVariable String id) {
        apiTaskService.pauseTask(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Task paused successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> executeTask(@PathVariable String id) {
        apiTaskService.executeTask(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Task executed successfully");
        return ResponseEntity.ok(response);
    }
}