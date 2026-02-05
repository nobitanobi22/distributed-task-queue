package com.taskqueue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "dead_letter_queue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "original_task_id")
    private String originalTaskId;
    
    @Column(name = "task_type")
    private String taskType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "retry_count")
    private Integer retryCount;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @PrePersist
    protected void onCreate() {
        failedAt = LocalDateTime.now();
    }
}
