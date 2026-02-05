package com.taskqueue.controller;

import com.taskqueue.dto.MetricsResponse;
import com.taskqueue.service.MetricsService;
import com.taskqueue.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {
    
    private final MetricsService metricsService;
    private final TaskExecutorService executorService;
    
    @GetMapping
    public ResponseEntity<MetricsResponse> getMetrics() {
        MetricsResponse metrics = metricsService.getMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/task-types")
    public ResponseEntity<List<String>> getSupportedTaskTypes() {
        List<String> taskTypes = executorService.getSupportedTaskTypes();
        return ResponseEntity.ok(taskTypes);
    }
}
