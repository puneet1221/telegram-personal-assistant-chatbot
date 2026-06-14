#!/bin/bash

# Docker Compose Startup Script for Linux/Mac

echo ""
echo "========================================"
echo "Telegram Personal Assistant Bot Setup"
echo "========================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed"
    echo "Please install Docker from: https://docs.docker.com/get-docker/"
    exit 1
fi

echo "[✓] Docker found"
echo ""

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "ERROR: Docker Compose is not installed"
    echo "Please install Docker Compose from: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "[✓] Docker Compose found"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "[!] .env file not found"
    echo "Creating .env from .env.example..."
    cp .env.example .env
    echo "[!] Please edit .env with your actual API keys"
    echo ""
    sleep 2
fi

echo "[*] Starting Docker containers..."
echo ""

# Start services
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start containers"
    exit 1
fi

echo ""
echo "[✓] Containers started successfully!"
echo ""
echo "========================================"
echo "Next Steps:"
echo "========================================"
echo ""
echo "1. Wait 30 seconds for services to initialize"
echo ""
echo "2. Pull Ollama model (if not already present):"
echo "   docker exec personal-assistant-ollama ollama pull nomic-embed-text"
echo ""
echo "3. Verify services are running:"
echo "   docker-compose ps"
echo ""
echo "4. View application logs:"
echo "   docker-compose logs -f app"
echo ""
echo "5. Access the bot at: http://localhost:8080"
echo ""
echo "6. MongoDB connection:"
echo "   mongodb://admin:YOUR_PASSWORD@localhost:27017/personal-assistant"
echo ""
echo "========================================"
echo ""
