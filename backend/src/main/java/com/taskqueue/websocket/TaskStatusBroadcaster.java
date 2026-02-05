package com.taskqueue.websocket;

import com.taskqueue.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskStatusBroadcaster {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void broadcastTaskUpdate(String taskId, TaskStatus status) {
        TaskStatusUpdate update = new TaskStatusUpdate(
            taskId, 
            status, 
            LocalDateTime.now()
        );
        
        // Broadcast to specific task subscribers
        messagingTemplate.convertAndSend("/topic/task/" + taskId, update);
        
        // Broadcast to all tasks subscribers (for dashboard)
        messagingTemplate.convertAndSend("/topic/tasks", update);
        
        log.debug("Broadcasted status update for task {}: {}", taskId, status);
    }
    
    @Data
    @AllArgsConstructor
    public static class TaskStatusUpdate {
        private String taskId;
        private TaskStatus status;
        private LocalDateTime timestamp;
    }
}
