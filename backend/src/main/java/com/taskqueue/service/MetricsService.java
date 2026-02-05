package com.taskqueue.service;

import com.taskqueue.dto.MetricsResponse;
import com.taskqueue.model.TaskStatus;
import com.taskqueue.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final TaskRepository taskRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public MetricsResponse getMetrics() {
        // Get metrics from Redis (fast) and fallback to DB (accurate)
        Long totalTasks = getMetricValue("metrics:tasks_submitted", 
                                         taskRepository.count());
        Long completedTasks = getMetricValue("metrics:tasks_completed", 
                                              taskRepository.countByStatus(TaskStatus.COMPLETED));
        Long failedTasks = getMetricValue("metrics:tasks_failed", 
                                          taskRepository.countByStatus(TaskStatus.FAILED));
        Long pendingTasks = taskRepository.countByStatus(TaskStatus.PENDING);
        Long processingTasks = taskRepository.countByStatus(TaskStatus.PROCESSING);
        
        Double avgProcessingTime = (Double) redisTemplate.opsForValue()
            .get("metrics:avg_processing_time");
        if (avgProcessingTime == null) {
            avgProcessingTime = taskRepository.getAverageProcessingTime();
            if (avgProcessingTime == null) avgProcessingTime = 0.0;
        }
        
        Map<String, Integer> queueSizes = new HashMap<>();
        queueSizes.put("high", getQueueSize("high"));
        queueSizes.put("medium", getQueueSize("medium"));
        queueSizes.put("low", getQueueSize("low"));
        
        double successRate = totalTasks > 0 
            ? (completedTasks * 100.0) / totalTasks 
            : 0.0;
        
        return MetricsResponse.builder()
            .totalTasks(totalTasks)
            .completedTasks(completedTasks)
            .failedTasks(failedTasks)
            .pendingTasks(pendingTasks)
            .processingTasks(processingTasks)
            .avgProcessingTime(avgProcessingTime)
            .queueSizes(queueSizes)
            .successRate(Math.round(successRate * 100.0) / 100.0)
            .build();
    }
    
    private Long getMetricValue(String redisKey, Long dbFallback) {
        Long value = (Long) redisTemplate.opsForValue().get(redisKey);
        return value != null ? value : dbFallback;
    }
    
    private Integer getQueueSize(String priority) {
        Long size = (Long) redisTemplate.opsForValue()
            .get("queue:size:" + priority);
        return size != null ? size.intValue() : 0;
    }
}
