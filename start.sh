#!/bin/bash

# Distributed Task Queue System - Startup Script
# This script helps you start all services easily

set -e

echo "🚀 Distributed Task Queue System - Startup Script"
echo "=================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi
print_status "Docker is installed"

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi
print_status "Docker Compose is installed"

if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17+ first."
    exit 1
fi
print_status "Java is installed"

if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven 3.6+ first."
    exit 1
fi
print_status "Maven is installed"

if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Please install Node.js 18+ first."
    exit 1
fi
print_status "Node.js is installed"

echo ""
echo "Starting services..."
echo ""

# Step 1: Start Docker services
print_status "Starting Docker services (PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana)..."
docker-compose up -d

echo "Waiting for services to be ready..."
sleep 10

# Check if services are running
if docker-compose ps | grep -q "Up"; then
    print_status "Docker services are running"
else
    print_error "Failed to start Docker services"
    exit 1
fi

# Step 2: Build and start backend
echo ""
print_status "Building backend..."
cd backend

if [ ! -f "pom.xml" ]; then
    print_error "Backend pom.xml not found. Are you in the correct directory?"
    exit 1
fi

mvn clean install -DskipTests
print_status "Backend built successfully"

echo ""
print_status "Starting backend on port 8080..."
mvn spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > ../backend.pid

cd ..

# Wait for backend to start
echo "Waiting for backend to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        print_status "Backend is running (PID: $BACKEND_PID)"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        print_error "Backend failed to start. Check backend.log for details."
        exit 1
    fi
done

# Step 3: Start frontend
echo ""
print_status "Setting up frontend..."
cd frontend

if [ ! -d "node_modules" ]; then
    print_status "Installing frontend dependencies..."
    npm install
fi

print_status "Starting frontend on port 3000..."
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../frontend.pid

cd ..

# Wait for frontend to start
echo "Waiting for frontend to start..."
sleep 5
print_status "Frontend is running (PID: $FRONTEND_PID)"

echo ""
echo "=================================================="
echo -e "${GREEN}🎉 All services started successfully!${NC}"
echo "=================================================="
echo ""
echo "📊 Access Points:"
echo "  • Frontend Dashboard:  http://localhost:3000"
echo "  • Backend API:         http://localhost:8080/api"
echo "  • RabbitMQ Management: http://localhost:15672 (admin/password)"
echo "  • Grafana:             http://localhost:3001 (admin/admin)"
echo "  • Prometheus:          http://localhost:9090"
echo ""
echo "📝 Process IDs:"
echo "  • Backend:  $BACKEND_PID"
echo "  • Frontend: $FRONTEND_PID"
echo ""
echo "🛑 To stop all services, run:"
echo "  ./stop.sh"
echo ""
echo "📋 Logs:"
echo "  • Backend:  tail -f backend.log"
echo "  • Frontend: tail -f frontend.log"
echo "  • Docker:   docker-compose logs -f"
echo ""
