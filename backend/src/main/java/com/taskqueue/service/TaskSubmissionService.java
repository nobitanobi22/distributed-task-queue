package com.taskqueue.service;

import com.taskqueue.config.RabbitMQConfig;
import com.taskqueue.dto.TaskRequest;
import com.taskqueue.dto.TaskResponse;
import com.taskqueue.model.Priority;
import com.taskqueue.model.Task;
import com.taskqueue.model.TaskStatus;
import com.taskqueue.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSubmissionService {
    
    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskExecutorService executorService;
    
    @Transactional
    public TaskResponse submitTask(TaskRequest request) {
        // Validate task type
        if (!executorService.isTaskTypeSupported(request.getTaskType())) {
            throw new IllegalArgumentException(
                "Unsupported task type: " + request.getTaskType()
            );
        }
        
        // Generate unique task ID
        String taskId = "task_" + UUID.randomUUID().toString().replace("-", "");
        
        // Create and persist task
        Task task = Task.builder()
            .taskId(taskId)
            .taskType(request.getTaskType())
            .priority(request.getPriority())
            .payload(request.getPayload())
            .status(TaskStatus.PENDING)
            .maxRetries(request.getMaxRetries())
            .scheduledAt(request.getScheduledAt())
            .retryCount(0)
            .build();
        
        taskRepository.save(task);
        
        // Cache task status in Redis (TTL: 1 hour)
        String redisKey = "task:status:" + taskId;
        redisTemplate.opsForHash().put(redisKey, "status", TaskStatus.PENDING.name());
        redisTemplate.opsForHash().put(redisKey, "taskType", task.getTaskType());
        redisTemplate.opsForHash().put(redisKey, "priority", task.getPriority().name());
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
        
        // Route to appropriate priority queue
        String queueName = getQueueByPriority(request.getPriority());
        rabbitTemplate.convertAndSend(queueName, task);
        
        // Update metrics
        redisTemplate.opsForValue().increment("metrics:tasks_submitted");
        redisTemplate.opsForValue().increment("metrics:tasks_" + request.getPriority().name().toLowerCase());
        
        // Update queue size
        redisTemplate.opsForValue().increment("queue:size:" + request.getPriority().name().toLowerCase());
        
        log.info("Task submitted successfully: taskId={}, type={}, priority={}", 
                 taskId, request.getTaskType(), request.getPriority());
        
        return buildTaskResponse(task);
    }
    
    private String getQueueByPriority(Priority priority) {
        return switch(priority) {
            case HIGH -> RabbitMQConfig.HIGH_PRIORITY_QUEUE;
            case MEDIUM -> RabbitMQConfig.MEDIUM_PRIORITY_QUEUE;
            case LOW -> RabbitMQConfig.LOW_PRIORITY_QUEUE;
        };
    }
    
    private TaskResponse buildTaskResponse(Task task) {
        return TaskResponse.builder()
            .taskId(task.getTaskId())
            .taskType(task.getTaskType())
            .priority(task.getPriority())
            .status(task.getStatus())
            .createdAt(task.getCreatedAt())
            .estimatedWaitTime(estimateWaitTime(task.getPriority()))
            .build();
    }
    
    private String estimateWaitTime(Priority priority) {
        // Simple estimation based on queue size
        String queueKey = "queue:size:" + priority.name().toLowerCase();
        Long queueSize = (Long) redisTemplate.opsForValue().get(queueKey);
        if (queueSize == null) queueSize = 0L;
        
        // Assume 2 seconds per task on average
        long waitSeconds = queueSize * 2;
        if (waitSeconds < 60) {
            return waitSeconds + "s";
        } else {
            return (waitSeconds / 60) + "m";
        }
    }
    
    public TaskResponse getTaskStatus(String taskId) {
        Task task = taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        
        return TaskResponse.builder()
            .taskId(task.getTaskId())
            .taskType(task.getTaskType())
            .priority(task.getPriority())
            .status(task.getStatus())
            .createdAt(task.getCreatedAt())
            .completedAt(task.getCompletedAt())
            .errorMessage(task.getErrorMessage())
            .retryCount(task.getRetryCount())
            .build();
    }
}
