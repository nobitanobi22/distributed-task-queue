#!/bin/bash

# Test script for Distributed Task Queue System

echo "🧪 Testing Distributed Task Queue System"
echo "========================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

test_passed=0
test_failed=0

run_test() {
    local test_name="$1"
    local command="$2"
    local expected_status="$3"
    
    echo -n "Testing: $test_name... "
    
    response=$(eval "$command" 2>&1)
    status=$?
    
    if [ $status -eq $expected_status ]; then
        echo -e "${GREEN}✓ PASSED${NC}"
        ((test_passed++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        echo "  Response: $response"
        ((test_failed++))
        return 1
    fi
}

echo "1. Backend Health Check"
run_test "Backend health endpoint" \
    "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/actuator/health" \
    0

echo ""
echo "2. Submit Test Tasks"

# Test EMAIL_SEND task
echo -n "Submitting EMAIL_SEND task... "
response=$(curl -s -X POST http://localhost:8080/api/tasks/submit \
    -H "Content-Type: application/json" \
    -d '{
        "taskType": "EMAIL_SEND",
        "priority": "HIGH",
        "payload": {
            "to": "test@example.com",
            "subject": "Test Email",
            "body": "This is a test email from the task queue system."
        },
        "maxRetries": 3
    }' 2>&1)

if echo "$response" | grep -q "taskId"; then
    task_id=$(echo "$response" | grep -o '"taskId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ SUCCESS${NC} (Task ID: $task_id)"
    ((test_passed++))
    
    # Wait a bit and check status
    echo "Waiting 3 seconds for task to process..."
    sleep 3
    
    echo -n "Checking task status... "
    status_response=$(curl -s http://localhost:8080/api/tasks/$task_id)
    
    if echo "$status_response" | grep -q "status"; then
        status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        echo -e "${GREEN}✓ Task Status: $status${NC}"
        ((test_passed++))
    else
        echo -e "${RED}✗ FAILED to get status${NC}"
        ((test_failed++))
    fi
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Response: $response"
    ((test_failed++))
fi

echo ""
# Test IMAGE_PROCESS task
echo -n "Submitting IMAGE_PROCESS task... "
response=$(curl -s -X POST http://localhost:8080/api/tasks/submit \
    -H "Content-Type: application/json" \
    -d '{
        "taskType": "IMAGE_PROCESS",
        "priority": "MEDIUM",
        "payload": {
            "imageUrl": "https://example.com/test-image.jpg",
            "operation": "resize",
            "width": 800,
            "height": 600
        },
        "maxRetries": 3
    }' 2>&1)

if echo "$response" | grep -q "taskId"; then
    task_id=$(echo "$response" | grep -o '"taskId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ SUCCESS${NC} (Task ID: $task_id)"
    ((test_passed++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((test_failed++))
fi

echo ""
# Test REPORT_GENERATE task
echo -n "Submitting REPORT_GENERATE task... "
response=$(curl -s -X POST http://localhost:8080/api/tasks/submit \
    -H "Content-Type: application/json" \
    -d '{
        "taskType": "REPORT_GENERATE",
        "priority": "LOW",
        "payload": {
            "reportType": "sales",
            "dateRange": "2025-01-01 to 2025-01-31"
        },
        "maxRetries": 3
    }' 2>&1)

if echo "$response" | grep -q "taskId"; then
    task_id=$(echo "$response" | grep -o '"taskId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ SUCCESS${NC} (Task ID: $task_id)"
    ((test_passed++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((test_failed++))
fi

echo ""
echo "3. Test Metrics Endpoint"
run_test "Fetch metrics" \
    "curl -s http://localhost:8080/api/metrics | grep -q 'totalTasks'" \
    0

echo ""
echo "4. Test Task Types Endpoint"
run_test "Fetch supported task types" \
    "curl -s http://localhost:8080/api/metrics/task-types | grep -q 'EMAIL_SEND'" \
    0

echo ""
echo "========================================"
echo "Test Results:"
echo -e "  ${GREEN}Passed: $test_passed${NC}"
echo -e "  ${RED}Failed: $test_failed${NC}"
echo "========================================"

if [ $test_failed -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed!${NC}"
    exit 1
fi
