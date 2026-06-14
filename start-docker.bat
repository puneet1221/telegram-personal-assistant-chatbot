@echo off
REM Docker Compose Startup Script for Windows

echo.
echo ========================================
echo Telegram Personal Assistant Bot Setup
echo ========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop from: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo [✓] Docker found
echo.

REM Check if docker-compose is available
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker Compose is not installed
    echo Please install Docker Desktop (includes Compose)
    pause
    exit /b 1
)

echo [✓] Docker Compose found
echo.

REM Check if .env file exists
if not exist .env (
    echo [!] .env file not found
    echo Creating .env from .env.example...
    copy .env.example .env
    echo [!] Please edit .env with your actual API keys
    echo.
    timeout /t 2
)

echo [*] Starting Docker containers...
echo.

REM Start services
docker-compose up -d

if errorlevel 1 (
    echo ERROR: Failed to start containers
    pause
    exit /b 1
)

echo.
echo [✓] Containers started successfully!
echo.
echo ========================================
echo Next Steps:
echo ========================================
echo.
echo 1. Wait 30 seconds for services to initialize
echo.
echo 2. Pull Ollama model (if not already present):
echo    docker exec personal-assistant-ollama ollama pull nomic-embed-text
echo.
echo 3. Verify services are running:
echo    docker-compose ps
echo.
echo 4. View application logs:
echo    docker-compose logs -f app
echo.
echo 5. Access the bot at: http://localhost:8080
echo.
echo 6. MongoDB connection:
echo    mongodb://admin:YOUR_PASSWORD@localhost:27017/personal-assistant
echo.
echo ========================================
echo.

pause
