# 🚀 Complete Setup Guide for Distributed Task Queue System

This guide will walk you through setting up and running the Distributed Task Queue System on your local machine.

## 📋 Table of Contents

1. [Prerequisites Installation](#prerequisites-installation)
2. [Project Setup](#project-setup)
3. [Running the Application](#running-the-application)
4. [Testing](#testing)
5. [Troubleshooting](#troubleshooting)
6. [Advanced Configuration](#advanced-configuration)

---

## 1. Prerequisites Installation

### Windows

#### Install Java 17+
1. Download from: https://adoptium.net/
2. Install and add to PATH
3. Verify: `java -version`

#### Install Maven
1. Download from: https://maven.apache.org/download.cgi
2. Extract and add to PATH
3. Verify: `mvn -version`

#### Install Node.js
1. Download from: https://nodejs.org/ (LTS version)
2. Install
3. Verify: `node -v` and `npm -v`

#### Install Docker Desktop
1. Download from: https://www.docker.com/products/docker-desktop
2. Install and start Docker Desktop
3. Verify: `docker --version` and `docker-compose --version`

### macOS

```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java
brew install openjdk@17
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Install Maven
brew install maven

# Install Node.js
brew install node

# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop
# Or use brew cask
brew install --cask docker
```

### Linux (Ubuntu/Debian)

```bash
# Update package list
sudo apt update

# Install Java 17
sudo apt install openjdk-17-jdk -y

# Install Maven
sudo apt install maven -y

# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# Install Docker
sudo apt install docker.io docker-compose -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Re-login for docker group changes to take effect
```

### Verify Installations

```bash
java -version      # Should show 17+
mvn -version       # Should show 3.6+
node -v            # Should show 18+
npm -v             # Should show 9+
docker --version   # Should show Docker version
docker-compose --version
```

---

## 2. Project Setup

### Option A: Using the Provided Files

If you already have all the files in the `distributed-task-queue` directory:

```bash
cd distributed-task-queue
ls -la  # Verify files are present
```

### Option B: Setting Up from Scratch

If you need to copy files from this conversation:

```bash
# Create project directory
mkdir distributed-task-queue
cd distributed-task-queue

# Copy all files provided in the conversation to this directory
# Maintain the directory structure as shown
```

### Verify Project Structure

```bash
tree -L 2  # or use 'ls -R' if tree is not available
```

You should see:
```
distributed-task-queue/
├── backend/
│   ├── src/
│   └── pom.xml
├── frontend/
│   ├── src/
│   └── package.json
├── docker-compose.yml
├── prometheus.yml
├── README.md
├── start.sh
├── stop.sh
└── test.sh
```

---

## 3. Running the Application

### Method 1: Using Automated Scripts (Recommended)

```bash
# Make scripts executable (if not already)
chmod +x start.sh stop.sh test.sh

# Start everything
./start.sh

# The script will:
# 1. Check prerequisites
# 2. Start Docker services
# 3. Build and start backend
# 4. Start frontend
# 5. Display access URLs

# To stop everything:
./stop.sh
```

### Method 2: Manual Step-by-Step

#### Step 1: Start Docker Services

```bash
# Start infrastructure services
docker-compose up -d

# Wait for services to be healthy (30-60 seconds)
docker-compose ps

# Verify services are running:
# - taskqueue-postgres should be Up
# - taskqueue-redis should be Up
# - taskqueue-rabbitmq should be Up
# - taskqueue-prometheus should be Up
# - taskqueue-grafana should be Up
```

#### Step 2: Start Backend

```bash
cd backend

# Build the project (first time only, or after code changes)
mvn clean install -DskipTests

# Option A: Run with Maven
mvn spring-boot:run

# Option B: Run JAR directly
java -jar target/distributed-task-queue-1.0.0.jar

# Backend will start on port 8080
# Look for: "Started DistributedTaskQueueApplication"
```

#### Step 3: Start Frontend (New Terminal)

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev

# Frontend will start on port 3000
```

---

## 4. Testing

### Quick Health Check

```bash
# Check if backend is running
curl http://localhost:8080/api/actuator/health

# Expected response:
# {"status":"UP"}
```

### Submit a Test Task

```bash
# Submit an email task
curl -X POST http://localhost:8080/api/tasks/submit \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "EMAIL_SEND",
    "priority": "HIGH",
    "payload": {
      "to": "test@example.com",
      "subject": "Test Email",
      "body": "Hello from Task Queue!"
    },
    "maxRetries": 3
  }'

# You should get a response with a taskId
# Example: {"taskId":"task_abc123...","status":"PENDING",...}
```

### Run Automated Tests

```bash
# Run the test script
./test.sh

# This will:
# 1. Test backend health
# 2. Submit tasks of each type
# 3. Check task status
# 4. Verify metrics
```

### Access the Web Interface

1. Open browser: http://localhost:3000
2. You should see the Dashboard with:
   - Metrics cards (Total, Completed, Failed, Pending)
   - Queue size chart
   - System stats

3. Try submitting a task:
   - Click "Submit Task" tab
   - Select task type
   - Fill in payload fields
   - Click "Submit Task"
   - Check "Task List" tab to see your task

---

## 5. Troubleshooting

### Problem: "Port already in use"

```bash
# Check what's using the ports
lsof -i :8080   # Backend
lsof -i :3000   # Frontend
lsof -i :5432   # PostgreSQL
lsof -i :6379   # Redis
lsof -i :5672   # RabbitMQ

# Kill the process if needed
kill -9 <PID>

# Or use different ports by modifying application.yml (backend)
# and vite.config.js (frontend)
```

### Problem: Docker containers won't start

```bash
# Check Docker status
docker ps -a

# Check logs
docker-compose logs postgres
docker-compose logs rabbitmq
docker-compose logs redis

# Restart all containers
docker-compose down
docker-compose up -d

# If issues persist, remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Problem: Backend compilation errors

```bash
cd backend

# Clean and rebuild
mvn clean
mvn install -DskipTests

# If dependency issues:
mvn dependency:purge-local-repository
mvn clean install -DskipTests
```

### Problem: Frontend won't start

```bash
cd frontend

# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# If port 3000 is busy, edit vite.config.js:
# Change: server: { port: 3000 }
# To:     server: { port: 3001 }
```

### Problem: Cannot connect to database

```bash
# Verify PostgreSQL is running
docker exec -it taskqueue-postgres psql -U admin -d taskqueue

# If connection refused:
docker-compose restart postgres

# Check backend logs for connection details
cat backend.log | grep -i "database\|postgres"
```

### Problem: Tasks not processing

```bash
# Check RabbitMQ
# Open: http://localhost:15672
# Login: admin / password
# Go to "Queues" tab
# You should see: queue.tasks.high, queue.tasks.medium, queue.tasks.low

# Check if workers are running
# Look in backend logs for "Worker picked up task"

# Restart backend if needed
./stop.sh
./start.sh
```

---

## 6. Advanced Configuration

### Change Database Settings

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskqueue
    username: your_username
    password: your_password
```

### Change Queue Priorities

Edit `backend/src/main/java/com/taskqueue/config/RabbitMQConfig.java`:

```java
// Increase high priority queue max priority
args.put("x-max-priority", 20);  // Default is 10
```

### Adjust Worker Concurrency

Edit `backend/src/main/java/com/taskqueue/service/TaskWorkerService.java`:

```java
@RabbitListener(queues = RabbitMQConfig.HIGH_PRIORITY_QUEUE, concurrency = "10-20")
// Increase from "5-10" to "10-20" for more workers
```

### Enable Production Mode

```bash
# Backend
cd backend
mvn clean package -DskipTests
java -jar target/distributed-task-queue-1.0.0.jar --spring.profiles.active=prod

# Frontend
cd frontend
npm run build
# Serve dist/ folder with nginx or your web server
```

---

## 📊 Monitoring URLs

Once everything is running:

- **Frontend Dashboard**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **RabbitMQ UI**: http://localhost:15672 (admin/password)
- **Grafana**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090

---

## 🎯 Next Steps

1. **Explore the Dashboard**: Monitor tasks in real-time
2. **Submit Different Tasks**: Try EMAIL_SEND, IMAGE_PROCESS, REPORT_GENERATE
3. **Check Metrics**: View success rates and processing times
4. **Monitor Queues**: Use RabbitMQ UI to see queue depths
5. **Add Custom Tasks**: Create your own TaskExecutor implementations

---

## 📞 Getting Help

If you encounter issues:

1. Check logs: `backend.log` and `frontend.log`
2. Check Docker logs: `docker-compose logs -f`
3. Review this guide's troubleshooting section
4. Check the main README.md for additional information

---

**Happy Task Queuing! 🚀**
