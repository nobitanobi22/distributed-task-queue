# ⚡ Quick Start Guide

**Get the Distributed Task Queue System running in 5 minutes!**

## Prerequisites Check

```bash
java -version      # Need 17+
mvn -version       # Need 3.6+
node -v            # Need 18+
docker --version   # Need Docker
```

If any are missing, see [SETUP_GUIDE.md](SETUP_GUIDE.md) for installation.

## 3-Step Startup

### 1️⃣ Start Infrastructure

```bash
docker-compose up -d
```

Wait 30 seconds for services to initialize.

### 2️⃣ Start Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Wait for "Started DistributedTaskQueueApplication" message.

### 3️⃣ Start Frontend (New Terminal)

```bash
cd frontend
npm install
npm run dev
```

## 🎉 You're Done!

Open http://localhost:3000 in your browser.

## Quick Test

```bash
curl -X POST http://localhost:8080/api/tasks/submit \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "EMAIL_SEND",
    "priority": "HIGH",
    "payload": {
      "to": "test@example.com",
      "subject": "Test",
      "body": "Hello!"
    },
    "maxRetries": 3
  }'
```

## Using Automated Scripts

Even easier:

```bash
chmod +x start.sh stop.sh test.sh
./start.sh          # Start everything
./test.sh           # Run tests
./stop.sh           # Stop everything
```

## Access URLs

- Frontend: http://localhost:3000
- Backend: http://localhost:8080/api
- RabbitMQ: http://localhost:15672 (admin/password)
- Grafana: http://localhost:3001 (admin/admin)

## Need Help?

- See [SETUP_GUIDE.md](SETUP_GUIDE.md) for detailed instructions
- See [README.md](README.md) for complete documentation
- Check logs: `backend.log`, `frontend.log`, or `docker-compose logs`
