# 🪟 Windows Setup Guide for Distributed Task Queue

## ✅ Step-by-Step Setup for Windows

### 1️⃣ Install Prerequisites

#### Install Java 17
```powershell
# Download and install from:
https://adoptium.net/temurin/releases/?version=17

# After installation, verify:
java -version
# Should show: openjdk version "17.x.x"
```

#### Install Node.js
```powershell
# Download and install from:
https://nodejs.org/en/download/

# After installation, verify:
node -v
npm -v
```

#### Install Docker Desktop
```powershell
# Download and install from:
https://www.docker.com/products/docker-desktop/

# After installation, start Docker Desktop
# Verify:
docker --version
docker-compose --version
```

---

### 2️⃣ Extract and Navigate to Project

```powershell
# Extract the zip file to Desktop
# Navigate to the project
cd C:\Users\YOUR_USERNAME\Desktop\distributed-task-queue
```

---

### 3️⃣ Start Infrastructure (Docker)

```powershell
# Start PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana
docker-compose up -d

# Wait 30 seconds for services to start

# Verify services are running:
docker-compose ps
```

You should see 5 services running (postgres, redis, rabbitmq, prometheus, grafana)

---

### 4️⃣ Start Backend (Java Spring Boot)

**Option A: Using Maven Wrapper (NO MAVEN INSTALL NEEDED!)**

```powershell
cd backend

# Build the project (first time only)
.\mvnw.cmd clean install -DskipTests

# Run the application
.\mvnw.cmd spring-boot:run
```

**Option B: If you installed Maven:**

```powershell
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

**Wait for this message:**
```
Started DistributedTaskQueueApplication in X.XXX seconds
```

**Keep this terminal open!**

---

### 5️⃣ Start Frontend (React) - NEW POWERSHELL WINDOW

Open a **NEW PowerShell window** and run:

```powershell
cd C:\Users\YOUR_USERNAME\Desktop\distributed-task-queue\frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

**Wait for this message:**
```
  ➜  Local:   http://localhost:3000/
```

---

### 6️⃣ Access the Application

Open your browser and go to:

- **Frontend Dashboard**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/actuator/health
- **RabbitMQ UI**: http://localhost:15672 (username: admin, password: password)

---

### 7️⃣ Test the System

**Option 1: Use the Web Interface**
1. Go to http://localhost:3000
2. Click "Submit Task" tab
3. Select "EMAIL_SEND"
4. Fill in the form
5. Click "Submit Task"
6. Go to "Task List" tab to see your task

**Option 2: Use PowerShell (curl alternative)**

```powershell
# Install curl for Windows (if not available)
# Or use Invoke-RestMethod:

$body = @{
    taskType = "EMAIL_SEND"
    priority = "HIGH"
    payload = @{
        to = "test@example.com"
        subject = "Test Email"
        body = "Hello from Task Queue!"
    }
    maxRetries = 3
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/submit" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

---

## 🛑 How to Stop Everything

### Stop Backend
- Press `Ctrl + C` in the backend terminal

### Stop Frontend
- Press `Ctrl + C` in the frontend terminal

### Stop Docker Services
```powershell
docker-compose down
```

---

## 🔧 Troubleshooting

### Issue: "mvnw.cmd is not recognized"

**Solution 1:** Use the full path:
```powershell
.\mvnw.cmd clean install -DskipTests
```

**Solution 2:** Install Maven properly:
- Download: https://maven.apache.org/download.cgi
- Extract to `C:\Program Files\apache-maven-3.9.6`
- Add to PATH: `C:\Program Files\apache-maven-3.9.6\bin`

### Issue: "Port 8080 is already in use"

```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual number)
taskkill /PID <PID> /F
```

### Issue: "Docker daemon is not running"

1. Open Docker Desktop application
2. Wait for it to fully start
3. Try `docker-compose up -d` again

### Issue: "Cannot connect to database"

```powershell
# Restart PostgreSQL container
docker-compose restart postgres

# Check logs
docker-compose logs postgres
```

### Issue: Java not found

```powershell
# Check if Java is installed
java -version

# If not found, install from:
https://adoptium.net/temurin/releases/?version=17

# Add to PATH if needed:
# System Properties > Environment Variables > Path
# Add: C:\Program Files\Eclipse Adoptium\jdk-17.x.x\bin
```

---

## 📊 What Should You See?

### Backend Terminal
```
Started DistributedTaskQueueApplication in 5.234 seconds
Worker worker_abc12345 ready
```

### Frontend Terminal
```
  VITE v5.0.8  ready in 1234 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

### Docker
```powershell
docker-compose ps
# All 5 services should show "Up"
```

---

## 🎯 Quick Commands Reference

```powershell
# Start everything
docker-compose up -d                    # Infrastructure
cd backend && .\mvnw.cmd spring-boot:run  # Backend
cd frontend && npm run dev               # Frontend (new window)

# Stop everything
Ctrl+C (in backend terminal)
Ctrl+C (in frontend terminal)
docker-compose down

# Check logs
docker-compose logs -f postgres
docker-compose logs -f rabbitmq
```

---

## ✅ Success Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] Node.js 18+ installed (`node -v`)
- [ ] Docker Desktop running (`docker ps`)
- [ ] Docker services started (`docker-compose ps` shows 5 services Up)
- [ ] Backend running (see "Started DistributedTaskQueueApplication")
- [ ] Frontend running (see "Local: http://localhost:3000")
- [ ] Can access http://localhost:3000 in browser
- [ ] Can submit a task via web interface
- [ ] Can see task in Task List

---

## 🎉 You're Done!

The system is now running. Go to http://localhost:3000 to use the dashboard!

## 📞 Need More Help?

If you encounter any issues:
1. Check the Troubleshooting section above
2. Check logs in the terminals
3. Restart Docker Desktop
4. Make sure all ports (3000, 5432, 6379, 5672, 8080, 15672) are free
