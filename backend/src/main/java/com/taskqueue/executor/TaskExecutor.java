package com.taskqueue.executor;

import java.util.Map;

public interface TaskExecutor {
    
    /**
     * Returns the task type this executor handles
     */
    String getTaskType();
    
    /**
     * Executes the task with the given payload
     * @param payload Task payload containing task-specific parameters
     * @throws Exception if task execution fails
     */
    void execute(Map<String, Object> payload) throws Exception;
}
