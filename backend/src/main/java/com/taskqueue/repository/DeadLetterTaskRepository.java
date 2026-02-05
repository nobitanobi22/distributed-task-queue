package com.taskqueue.repository;

import com.taskqueue.model.DeadLetterTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeadLetterTaskRepository extends JpaRepository<DeadLetterTask, Long> {
    
    List<DeadLetterTask> findByOriginalTaskId(String originalTaskId);
    
    List<DeadLetterTask> findByFailedAtBetween(LocalDateTime start, LocalDateTime end);
    
    Long countByTaskType(String taskType);
}
