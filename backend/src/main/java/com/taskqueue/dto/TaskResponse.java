package com.taskqueue.dto;

import com.taskqueue.model.Priority;
import com.taskqueue.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private String taskId;
    private String taskType;
    private Priority priority;
    private TaskStatus status;
    private Integer queuePosition;
    private String estimatedWaitTime;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private Integer retryCount;
}
