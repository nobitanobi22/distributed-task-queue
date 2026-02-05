package com.taskqueue.controller;

import com.taskqueue.dto.TaskRequest;
import com.taskqueue.dto.TaskResponse;
import com.taskqueue.model.Task;
import com.taskqueue.model.TaskStatus;
import com.taskqueue.repository.TaskRepository;
import com.taskqueue.service.TaskSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskSubmissionService taskSubmissionService;
    private final TaskRepository taskRepository;
    
    @PostMapping("/submit")
    public ResponseEntity<TaskResponse> submitTask(@Valid @RequestBody TaskRequest request) {
        log.info("Received task submission request: type={}, priority={}", 
                 request.getTaskType(), request.getPriority());
        
        try {
            TaskResponse response = taskSubmissionService.submitTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid task submission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting task", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskStatus(@PathVariable String taskId) {
        try {
            TaskResponse response = taskSubmissionService.getTaskStatus(taskId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByStatus(status, pageRequest);
        } else {
            tasks = taskRepository.findAll(pageRequest);
        }
        return ResponseEntity.ok(tasks);
    }
    
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> cancelTask(@PathVariable String taskId) {
        try {
            Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
            
            if (task.getStatus() == TaskStatus.PENDING) {
                task.setStatus(TaskStatus.CANCELLED);
                taskRepository.save(task);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
