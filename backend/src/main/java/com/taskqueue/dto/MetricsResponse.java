package com.taskqueue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    
    private Long totalTasks;
    private Long completedTasks;
    private Long failedTasks;
    private Long pendingTasks;
    private Long processingTasks;
    private Double avgProcessingTime;
    private Map<String, Integer> queueSizes;
    private Double successRate;
}
