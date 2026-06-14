# Docker Compose Setup Guide

## Services Configured

### 1. **MongoDB** (Port: 27017)
- Database for storing users, expenses, habits, reminders
- Persistent volume: `mongodb_data`
- Health checks enabled
- Authentication enabled via environment variables

### 2. **Ollama** (Port: 11434)
- Local LLM service for embeddings
- Model: `nomic-embed-text`
- Persistent volume: `ollama_data`
- Health checks enabled

### 3. **Spring Boot Application** (Port: 8080)
- Telegram Personal Assistant Bot
- Depends on MongoDB and Ollama health checks
- Integrated with all external APIs

---

## Getting Started

### Step 1: Setup Environment Variables
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your actual values
# Required variables:
# - TELEGRAM_TOKEN: Your Telegram bot token
# - GROQ_API_KEY: Your Groq API key
# - WEATHER_API_KEY: Your weather API key
# - NEWS_API_KEY: Your news API key
# - WEBHOOK_URL: Your webhook URL
# - MONGO_PASSWORD: Secure password for MongoDB
```

### Step 2: Pull Required Ollama Models
```bash
# After starting containers, pull the embedding model
docker exec personal-assistant-ollama ollama pull nomic-embed-text

# Optional: Pull chat models (e.g., llama3)
docker exec personal-assistant-ollama ollama pull llama3
```

### Step 3: Start All Services
```bash
# Start all services in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f app
docker-compose logs -f mongodb
docker-compose logs -f ollama
```

### Step 4: Verify Services
```bash
# Check all running containers
docker-compose ps

# Test MongoDB connection
docker exec personal-assistant-mongodb mongosh mongodb://admin:password@localhost:27017/test

# Test Ollama
curl http://localhost:11434/api/tags

# Test Spring Boot app
curl http://localhost:8080/actuator/health
```

---

## Common Commands

### Stop Services
```bash
docker-compose down
```

### Stop and Remove Volumes (⚠️ Deletes data)
```bash
docker-compose down -v
```

### Rebuild Docker Image
```bash
docker-compose build --no-cache
docker-compose up -d
```

### View Logs in Real-time
```bash
docker-compose logs -f
```

### Execute Commands in Container
```bash
# Access MongoDB shell
docker exec -it personal-assistant-mongodb mongosh

# Access Ollama commands
docker exec personal-assistant-ollama ollama list

# View app logs
docker logs personal-assistant-bot
```

---

## Database Initialization

### MongoDB Credentials
- **Username**: `admin` (default, change in .env)
- **Password**: Set in `.env` (MONGO_PASSWORD)
- **Database**: `personal-assistant`
- **Connection String**: `mongodb://admin:password@localhost:27017/personal-assistant?authSource=admin`

### MongoDB Collections (Auto-created)
The application will automatically create these collections:
- `users`
- `expenses`
- `habits`
- `habitLogs`
- `reminders`
- `vector_store` (for RAG embeddings)

---

## Troubleshooting

### Service won't start
```bash
# Check logs
docker-compose logs app

# Verify network connectivity
docker network inspect <network-name>
```

### MongoDB connection failed
```bash
# Verify MongoDB is running
docker-compose ps mongodb

# Test connection
docker exec personal-assistant-mongodb mongosh
```

### Ollama model not available
```bash
# List available models
docker exec personal-assistant-ollama ollama list

# Pull required model
docker exec personal-assistant-ollama ollama pull nomic-embed-text
```

### Port already in use
- Change ports in `docker-compose.yml`
- Or kill process using the port:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

---

## Production Deployment Notes

For production, consider:

1. **Use external MongoDB service** (MongoDB Atlas)
2. **Add resource limits** (uncomment in docker-compose.yml)
3. **Use secrets management** instead of .env files
4. **Set `restart: on-failure` with max retry count**
5. **Add logging drivers** for centralized logging
6. **Use separate compose files** for dev/prod
7. **Implement SSL/TLS** for secure communication
8. **Setup automated backups** for MongoDB

---

## Network Architecture

```
┌─────────────────────────────────────────┐
│      Docker Bridge Network               │
│   (assistant-network)                   │
│                                         │
│  ┌────────────┐  ┌────────────┐       │
│  │  MongoDB   │  │   Ollama   │       │
│  │ :27017     │  │ :11434     │       │
│  └────────────┘  └────────────┘       │
│         ▲              ▲               │
│         └──────┬───────┘               │
│                │                       │
│         ┌──────▼──────┐                │
│         │ Spring Boot  │                │
│         │  App :8080   │                │
│         └──────────────┘                │
│                                         │
└─────────────────────────────────────────┘
         │
         │ External Access
         ▼
    Host Machine :8080
```

---

## Next Steps

1. ✅ Setup `.env` file with your API keys
2. ✅ Run `docker-compose up -d`
3. ✅ Pull Ollama models
4. ✅ Verify all services are running: `docker-compose ps`
5. ✅ Start using the bot!

