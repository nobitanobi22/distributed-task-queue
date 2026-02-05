package com.taskqueue.dto;

import com.taskqueue.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    
    @NotBlank(message = "Task type is required")
    private String taskType;
    
    @NotNull(message = "Priority is required")
    private Priority priority;
    
    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;
    
    private LocalDateTime scheduledAt;
    
    private Integer maxRetries = 3;
}
