# Save this as: fix-code.ps1 in the backend folder

# Fix TaskController.java - replace the getTasks method
$taskControllerPath = "src\main\java\com\taskqueue\controller\TaskController.java"
$content = Get-Content $taskControllerPath -Raw

$oldMethod = '@GetMapping[\s\S]*?return ResponseEntity\.ok\(tasks\);\s*\}'
$newMethod = @'
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
'@

$content = $content -replace $oldMethod, $newMethod
Set-Content $taskControllerPath -Value $content

Write-Host "Files fixed! Now run: .\mvnw.cmd clean install -DskipTests"
