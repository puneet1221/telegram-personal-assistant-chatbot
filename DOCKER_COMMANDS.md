# Docker Compose Quick Commands

## 🚀 Start Services
```bash
docker-compose up -d                  # Start all services in background
docker-compose up                     # Start in foreground (see logs)
./start-docker.sh                     # (Linux/Mac) Automated startup
./start-docker.bat                    # (Windows) Automated startup
```

## 🛑 Stop Services
```bash
docker-compose down                   # Stop all services
docker-compose down -v                # Stop and remove volumes (⚠️ DELETES DATA)
docker-compose stop                   # Stop without removing
docker-compose restart                # Restart all services
docker-compose restart app            # Restart specific service
```

## 📊 Monitor Services
```bash
docker-compose ps                     # List all running containers
docker-compose logs                   # View logs from all services
docker-compose logs -f                # Follow logs in real-time
docker-compose logs -f app            # Follow logs for specific service
docker-compose logs -f --tail=50      # View last 50 lines
docker-compose logs app --since=1h    # Logs from last hour
```

## 🔧 Build & Update
```bash
docker-compose build                  # Build images
docker-compose build --no-cache       # Build without cache
docker-compose up -d --build          # Build and start
```

## 💾 Database Operations

### MongoDB
```bash
# Access MongoDB shell
docker exec -it personal-assistant-mongodb mongosh

# Backup database
docker exec personal-assistant-mongodb mongodump --out /dump

# Restore database
docker exec -i personal-assistant-mongodb mongorestore --archive < backup.archive

# View database size
docker exec personal-assistant-mongodb db.stats()

# List databases
docker exec personal-assistant-mongodb mongosh --eval "show databases"
```

### Example MongoDB Commands Inside Shell
```javascript
// Switch database
use personal-assistant

// View collections
show collections

// Count documents
db.users.countDocuments()
db.expenses.countDocuments()
db.habits.countDocuments()

// View sample document
db.users.findOne()
db.expenses.findOne()

// Query by user
db.expenses.find({ userId: "user_id_here" })
```

## 🤖 Ollama Operations
```bash
# List available models
docker exec personal-assistant-ollama ollama list

# Pull a model
docker exec personal-assistant-ollama ollama pull nomic-embed-text
docker exec personal-assistant-ollama ollama pull llama3

# Run model interactively
docker exec -it personal-assistant-ollama ollama run nomic-embed-text

# View Ollama logs
docker logs personal-assistant-ollama
```

## 🔌 Application Operations
```bash
# View Spring Boot logs
docker logs -f personal-assistant-bot

# Check application health
curl http://localhost:8080/actuator/health

# View application metrics
curl http://localhost:8080/actuator/metrics

# Access Swagger UI (if enabled)
curl http://localhost:8080/swagger-ui.html
```

## 🧪 Testing Connectivity

### Test MongoDB
```bash
# From host
mongosh "mongodb://admin:password@localhost:27017/personal-assistant?authSource=admin"

# From another container
docker exec personal-assistant-bot mongosh "mongodb://admin:password@mongodb:27017/personal-assistant?authSource=admin"
```

### Test Ollama
```bash
# Check if running
curl http://localhost:11434/api/tags

# Detailed status
curl -v http://localhost:11434/api/tags
```

### Test Spring Boot
```bash
# Health check
curl http://localhost:8080/actuator/health

# Ready probe
curl http://localhost:8080/actuator/health/readiness

# Liveness probe
curl http://localhost:8080/actuator/health/liveness
```

## 🐛 Debugging

### View container details
```bash
docker-compose config                 # View resolved docker-compose config
docker inspect personal-assistant-bot # Detailed container info
docker inspect personal-assistant-mongodb
```

### Check network
```bash
docker network ls                     # List networks
docker network inspect assistant-network  # Inspect network
```

### Execute commands in container
```bash
docker exec personal-assistant-bot bash              # Open bash shell
docker exec personal-assistant-bot ps aux            # View processes
docker exec personal-assistant-app env              # View environment variables
```

### Check resource usage
```bash
docker stats                          # Real-time resource usage
docker stats personal-assistant-bot
```

## 🔄 Clean Up

### Remove unused resources
```bash
docker system prune                   # Remove unused images, containers, networks
docker volume prune                   # Remove unused volumes
docker image prune                    # Remove unused images
```

### Full cleanup (⚠️ CAREFUL)
```bash
docker-compose down -v                # Remove everything for this compose
docker system prune -a                # Remove all unused resources
docker volume rm $(docker volume ls -q)  # Remove all volumes
```

## 📝 Useful Aliases (Add to .bashrc or .zshrc)

```bash
# Start all services
alias dc-up="docker-compose up -d"

# Stop all services
alias dc-down="docker-compose down"

# View logs
alias dc-logs="docker-compose logs -f"

# Restart app
alias dc-restart="docker-compose restart app"

# MongoDB shell
alias mongo-shell="docker exec -it personal-assistant-mongodb mongosh"

# App shell
alias app-shell="docker exec -it personal-assistant-bot bash"

# Health check
alias dc-health="curl -s http://localhost:8080/actuator/health | jq"
```

## 🆘 Troubleshooting

### Port already in use
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Container won't start
```bash
docker logs personal-assistant-bot
docker logs personal-assistant-mongodb
docker logs personal-assistant-ollama
```

### Network issues
```bash
docker network inspect assistant-network
docker exec personal-assistant-bot ping mongodb
docker exec personal-assistant-bot curl http://ollama:11434/api/tags
```

### View environment variables
```bash
docker exec personal-assistant-bot env | sort
```

---

**Pro Tip**: Create a `.env.local` file for local development overrides:
```bash
# .env.local
MONGO_PASSWORD=dev_password
TELEGRAM_TOKEN=your_test_token
```

Then use: `docker-compose --env-file .env.local up -d`
