package com.taskqueue.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taskqueue.model.Task;
import com.taskqueue.model.TaskStatus;
import com.taskqueue.model.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    Optional<Task> findByTaskId(String taskId);
    
    List<Task> findByStatus(TaskStatus status);
    
    List<Task> findByPriority(Priority priority);
    
    List<Task> findByStatusAndPriority(TaskStatus status, Priority priority);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Long countByStatus(TaskStatus status);
    
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, t.startedAt, t.completedAt)) FROM Task t WHERE t.status = 'COMPLETED'")
    Double getAverageProcessingTime();
    
    List<Task> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.scheduledAt <= :now ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findScheduledTasks(LocalDateTime now);
}
