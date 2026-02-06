# Distributed Task Queue System

A production-grade distributed task queue with priority scheduling, automatic retry logic, and dead letter queue management. Built with Java Spring Boot, RabbitMQ, Redis, PostgreSQL, and React.

## Features

-  **Priority Queue System** - HIGH, MIUM, LOW priority queues with RabbitMQ
- **Automatic Retry Logic** - Exponential backoff with configurable max retries
- **Dead Letter Queue** - Failed tasks after max retries moved to DLQ
- **Real-time Monitoring** - WebSocket updates for task status
- **Metrics Dashboard** - Success rate, processing time, queue depths
- **Extensible Architecture** - Plugin-based task executors
- **Production Ready** - Docker, Prometheus metrics, comprehensive testing

## Architecture

```
Frontend (React + MUI) → REST API (Spring Boot) → RabbitMQ Queues → Workers
                              ↓                         ↓
                         PostgreSQL              Redis Cache
                              ↓
                    Prometheus + Grafana (Metrics)
```

## 📋 Prerequisites

- **Java 17+** (JDK)
- **Maven 3.6+**
- **Node.js 18+** and npm
- **Docker & Docker Compose**
- **Git**

## 🚀 Quick Start (Local Setup)

### Step 1: Clone the Repository

```bash
# If you don't have the project yet, create directory structure
mkdir distributed-task-queue
cd distributed-task-queue
```

### Step 2: Start Infrastructure with Docker Compose

```bash
# Start PostgreSQL, Redis, RabbitMQ, Prometheus, and Grafana
docker-compose up -d


### Step 3: Build and Run Backend

```bash
cd backend

# Build the project
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
# java -jar target/distributed-task-queue-1.0.0.jar

# Backend will start on http://localhost:8080
```

### Step 4: Setup and Run Frontend

```bash
cd ../frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Frontend will start on http://localhost:3000
```

## Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend Dashboard | http://localhost:3000 | - |
| Backend API | http://localhost:8080/api | - |
| RabbitMQ Management | http://localhost:15672 | admin / password |
| Grafana | http://localhost:3001 | admin / admin |
| Prometheus | http://localhost:9090 | - |

## 📚 Project Structure

```
distributed-task-queue/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/taskqueue/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── executor/        # Task executors
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # Data repositories
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── websocket/       # WebSocket handlers
│   │   │   │   └── DistributedTaskQueueApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/                    # Unit & integration tests
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/              # React components
│   │   ├── pages/                   # Page components
│   │   ├── services/                # API services
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
├── prometheus.yml
└── README.md
```








## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

MIT License

## 👨‍💻 Author

Kumari Ankita
- GitHub: [@nobitanobi22](https://github.com/nobitanobi22)
- LinkedIn: [Kumari Ankita](https://www.linkedin.com/in/kumari-ankita-31b2bb250/)
