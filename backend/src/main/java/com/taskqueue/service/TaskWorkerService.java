package com.taskqueue.service;

import com.taskqueue.config.RabbitMQConfig;
import com.taskqueue.model.DeadLetterTask;
import com.taskqueue.model.Task;
import com.taskqueue.model.TaskStatus;
import com.taskqueue.repository.DeadLetterTaskRepository;
import com.taskqueue.repository.TaskRepository;
import com.taskqueue.websocket.TaskStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskWorkerService {
    
    private final TaskRepository taskRepository;
    private final DeadLetterTaskRepository deadLetterTaskRepository;
    private final TaskExecutorService executorService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final TaskStatusBroadcaster statusBroadcaster;
    
    private final String workerId = "worker_" + UUID.randomUUID().toString().substring(0, 8);
    
    @RabbitListener(queues = RabbitMQConfig.HIGH_PRIORITY_QUEUE, concurrency = "5-10")
    public void processHighPriorityTask(Task task) {
        processTask(task, "HIGH");
    }
    
    @RabbitListener(queues = RabbitMQConfig.MEDIUM_PRIORITY_QUEUE, concurrency = "3-5")
    public void processMediumPriorityTask(Task task) {
        processTask(task, "MEDIUM");
    }
    
    @RabbitListener(queues = RabbitMQConfig.LOW_PRIORITY_QUEUE, concurrency = "1-3")
    public void processLowPriorityTask(Task task) {
        processTask(task, "LOW");
    }
    
    @Transactional
    public void processTask(Task task, String priority) {
        long startTime = System.currentTimeMillis();
        String taskId = task.getTaskId();
        
        log.info("Worker {} picked up task: {} from {} priority queue", 
                 workerId, taskId, priority);
        
        try {
            // Update queue size
            redisTemplate.opsForValue().decrement("queue:size:" + priority.toLowerCase());
            
            // Update task status to PROCESSING
            updateTaskStatus(task, TaskStatus.PROCESSING);
            task.setStartedAt(LocalDateTime.now());
            task.setWorkerId(workerId);
            taskRepository.save(task);
            
            // Broadcast status update
            statusBroadcaster.broadcastTaskUpdate(taskId, TaskStatus.PROCESSING);
            
            // Execute the actual task logic
            executorService.execute(task.getTaskType(), task.getPayload());
            
            // Mark as COMPLETED
            updateTaskStatus(task, TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            
            // Broadcast completion
            statusBroadcaster.broadcastTaskUpdate(taskId, TaskStatus.COMPLETED);
            
            // Record metrics
            long duration = System.currentTimeMillis() - startTime;
            recordSuccessMetrics(task, duration);
            
            log.info("Task {} completed successfully by worker {} in {}ms", 
                     taskId, workerId, duration);
            
        } catch (Exception e) {
            log.error("Task {} failed: {}", taskId, e.getMessage(), e);
            handleTaskFailure(task, e, priority);
        }
    }
    
    private void handleTaskFailure(Task task, Exception e, String priority) {
        task.setRetryCount(task.getRetryCount() + 1);
        task.setErrorMessage(e.getMessage());
        
        if (task.getRetryCount() < task.getMaxRetries()) {
            // Retry with exponential backoff
            log.info("Retrying task {} (attempt {}/{})", 
                     task.getTaskId(), task.getRetryCount() + 1, task.getMaxRetries());
            
            updateTaskStatus(task, TaskStatus.PENDING);
            taskRepository.save(task);
            
            // Requeue task with delay
            String queueName = getQueueByPriority(priority);
            
            // Calculate backoff delay (2^retryCount seconds)
            long delayMs = (long) Math.pow(2, task.getRetryCount()) * 1000;
            
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            rabbitTemplate.convertAndSend(queueName, task);
            
            // Broadcast retry status
            statusBroadcaster.broadcastTaskUpdate(task.getTaskId(), TaskStatus.PENDING);
            
        } else {
            // Move to Dead Letter Queue
            log.error("Task {} failed after {} attempts, moving to DLQ", 
                      task.getTaskId(), task.getMaxRetries());
            
            updateTaskStatus(task, TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            
            // Save to DLQ table
            moveToDeadLetterQueue(task, e);
            
            // Broadcast failure
            statusBroadcaster.broadcastTaskUpdate(task.getTaskId(), TaskStatus.FAILED);
            
            // Record failure metrics
            recordFailureMetrics(task);
        }
    }
    
    private void updateTaskStatus(Task task, TaskStatus status) {
        task.setStatus(status);
        
        // Update Redis cache
        String redisKey = "task:status:" + task.getTaskId();
        redisTemplate.opsForHash().put(redisKey, "status", status.name());
        
        if (status == TaskStatus.PROCESSING) {
            redisTemplate.opsForHash().put(redisKey, "workerId", workerId);
        }
    }
    
    private void moveToDeadLetterQueue(Task task, Exception e) {
        DeadLetterTask dlqTask = DeadLetterTask.builder()
            .originalTaskId(task.getTaskId())
            .taskType(task.getTaskType())
            .payload(task.getPayload())
            .failureReason(e.getMessage())
            .retryCount(task.getRetryCount())
            .build();
        
        deadLetterTaskRepository.save(dlqTask);
        
        // Increment failed tasks metric
        redisTemplate.opsForValue().increment("metrics:tasks_failed");
    }
    
    private void recordSuccessMetrics(Task task, long durationMs) {
        redisTemplate.opsForValue().increment("metrics:tasks_completed");
        
        // Update average processing time
        String avgKey = "metrics:avg_processing_time";
        Double currentAvg = (Double) redisTemplate.opsForValue().get(avgKey);
        Long completedCount = (Long) redisTemplate.opsForValue().get("metrics:tasks_completed");
        
        if (currentAvg == null) currentAvg = 0.0;
        if (completedCount == null) completedCount = 1L;
        
        double newAvg = ((currentAvg * (completedCount - 1)) + durationMs) / completedCount;
        redisTemplate.opsForValue().set(avgKey, newAvg);
    }
    
    private void recordFailureMetrics(Task task) {
        redisTemplate.opsForValue().increment("metrics:tasks_failed");
        redisTemplate.opsForValue().increment("metrics:tasks_failed_" + task.getTaskType());
    }
    
    private String getQueueByPriority(String priority) {
        return switch(priority) {
            case "HIGH" -> RabbitMQConfig.HIGH_PRIORITY_QUEUE;
            case "MEDIUM" -> RabbitMQConfig.MEDIUM_PRIORITY_QUEUE;
            case "LOW" -> RabbitMQConfig.LOW_PRIORITY_QUEUE;
            default -> RabbitMQConfig.MEDIUM_PRIORITY_QUEUE;
        };
    }
}
