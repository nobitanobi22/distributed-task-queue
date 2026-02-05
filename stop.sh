#!/bin/bash

# Distributed Task Queue System - Stop Script

set -e

echo "🛑 Stopping Distributed Task Queue System..."
echo "============================================"

# Colors
GREEN='\033[0;32m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

# Stop backend
if [ -f backend.pid ]; then
    BACKEND_PID=$(cat backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "Stopping backend (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        rm backend.pid
        print_status "Backend stopped"
    fi
fi

# Stop frontend
if [ -f frontend.pid ]; then
    FRONTEND_PID=$(cat frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo "Stopping frontend (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        rm frontend.pid
        print_status "Frontend stopped"
    fi
fi

# Stop Docker services
echo "Stopping Docker services..."
docker-compose down
print_status "Docker services stopped"

echo ""
echo "============================================"
echo -e "${GREEN}✓ All services stopped successfully!${NC}"
echo "============================================"
