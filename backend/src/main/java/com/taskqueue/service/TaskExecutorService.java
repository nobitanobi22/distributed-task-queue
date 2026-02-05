package com.taskqueue.service;

import com.taskqueue.executor.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TaskExecutorService {
    
    private final Map<String, TaskExecutor> executors = new HashMap<>();
    
    @Autowired
    public TaskExecutorService(List<TaskExecutor> executorList) {
        // Auto-register all executor implementations
        executorList.forEach(executor -> {
            executors.put(executor.getTaskType(), executor);
            log.info("Registered executor for task type: {}", executor.getTaskType());
        });
    }
    
    public void execute(String taskType, Map<String, Object> payload) throws Exception {
        TaskExecutor executor = executors.get(taskType);
        
        if (executor == null) {
            throw new UnsupportedOperationException(
                "No executor found for task type: " + taskType
            );
        }
        
        log.info("Executing task type: {} with executor: {}", taskType, executor.getClass().getSimpleName());
        executor.execute(payload);
    }
    
    public boolean isTaskTypeSupported(String taskType) {
        return executors.containsKey(taskType);
    }
    
    public List<String> getSupportedTaskTypes() {
        return List.copyOf(executors.keySet());
    }
}
