# 🤖 Personal Assistant Telegram Bot

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=java" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-green?style=for-the-badge&logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Telegram-Bot-blue?style=for-the-badge&logo=telegram" alt="Telegram">
  <img src="https://img.shields.io/badge/MongoDB-4.4+-green?style=for-the-badge&logo=mongodb" alt="MongoDB">
  <img src="https://img.shields.io/badge/AI-Groq-orange?style=for-the-badge" alt="Groq AI">
</p>

> A powerful AI-powered Telegram bot that helps you manage expenses, reminders, habits, and more — all through natural conversation. Features intelligent intent parsing and RAG-based document Q&A capabilities.

---

## ✨ Features

### 💰 Expense Management
- Track daily expenses with categories (food, transport, shopping, health, entertainment)
- View, edit, and delete expense records
- Monthly spending summaries

### ⏰ Smart Reminders
- One-time reminders with date/time scheduling
- Recurring reminders (daily, weekly)
- Automatic cleanup of past reminders

### 📅 Habit Tracking
- Create and manage personal habits
- Daily habit completion logging
- Visual progress tracking

### 🌤️ Weather Updates
- Real-time weather information for any city
- Instant forecasts via inline buttons

### 📰 News Feed
- Category-based news (World, Sports, Tech, Business, Entertainment, Health)
- Top stories at a glance

### 📄 Document Q&A (RAG)
- Upload documents (PDF, TXT, DOCX)
- AI-powered semantic search over your documents
- Ask questions and get contextual answers

### 💬 AI Chat Integration
- Intelligent intent parsing using Groq AI
- Natural language processing for commands
- Context-aware responses

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Telegram User                             │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  PersonalAssistantBot                           │
│                  (TelegramWebhookBot)                           │
└─────────────────────────────┬───────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌──────────┐   ┌──────────┐    ┌──────────┐
        │ Handlers │   │ Services │    │   AI     │
        │ (Chain)  │   │  Layer   │    │  Layer   │
        └──────────┘   └──────────┘    └──────────┘
                           │
                           ▼
                    ┌──────────┐
                    │ MongoDB  │
                    │  Atlas   │
                    └──────────┘
```

### Design Patterns Used
- **Handler Chain Pattern** — Modular message handling
- **Service Layer** — Business logic separation
- **Strategy Pattern** — Different handlers for different message types

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.5 |
| **Language** | Java 17 |
| **Database** | MongoDB Atlas |
| **Bot API** | Telegram Bots API (telegrambots-spring-boot-starter) |
| **AI/ML** | Groq API (LLama 3.3), Ollama (Embeddings) |
| **RAG** | Spring AI, Apache Tika, Vector Store |
| **Scheduler** | Quartz Scheduler |
| **Build Tool** | Maven |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/project/personal_assistant/
│   │   ├── PersonalAssistantApplication.java    # Main entry point
│   │   ├── bot/
│   │   │   ├── PersonalAssistantBot.java         # Telegram bot implementation
│   │   │   ├── BotConfig.java                     # Bot configuration
│   │   │   ├── DailySummaryJob.java               # Scheduled daily summary
│   │   │   ├── ReminderJob.java                   # Reminder processing job
│   │   │   └── handler/                           # Message handlers
│   │   │       ├── MessageHandler.java           # Handler interface
│   │   │       ├── ExpenseHandler.java            # Expense processing
│   │   │       ├── ReminderHandler.java          # Reminder processing
│   │   │       ├── HabitHandler.java              # Habit tracking
│   │   │       ├── FileUploadHandler.java         # Document upload
│   │   │       ├── QnAHandler.java                # RAG-based Q&A
│   │   │       └── ... (15+ handlers)
│   │   ├── model/                                 # Data models
│   │   │   ├── Expense.java, Reminder.java, Habit.java, User.java
│   │   ├── repo/                                  # MongoDB repositories
│   │   ├── service/                               # Business logic
│   │   │   ├── GroqChatService.java               # AI intent parsing
│   │   │   ├── RAGService.java                    # Document Q&A
│   │   │   ├── FileProcessorService.java          # Tika file processing
│   │   │   ├── ExpenseService.java, HabitService.java, etc.
│   │   └── controllers/
│   │       ├── WebhookController.java             # Telegram webhooks
│   │       └── ExpenseController.java             # REST endpoints
│   └── resources/
│       └── application.properties                 # Configuration
└── test/                                          # Unit tests
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB Atlas account
- Telegram Bot Token (via @BotFather)
- Groq API Key

### Environment Variables

Create a `.env` file or set these system properties:

```properties
# Database
dburl=mongodb+srv://<username>:<password>@cluster.mongodb.net/

# Telegram Bot
token=your_telegram_bot_token
webHookUrl=https://your-ngrok-url.ngrok-free.app

# AI APIs
groqApiKey=your_groq_api_key
geminiApiKey=your_gemini_api_key

# External APIs
weatherApiKey=your_weather_api_key
newsApiKey=your_news_api_key
```

### Build & Run

```bash
# Build the project
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run
```

Or use the provided run script:
```bash
run.cmd
```

### Setting Up Telegram Webhook

1. Start ngrok: `ngrok http 8080`
2. Set webhook URL in application.properties
3. Your bot is ready to use!

---

## 📱 Bot Commands

| Command | Description |
|---------|-------------|
| `/start` | Launch the bot and show main menu |
| `/done` | Return to main menu from any section |
| Natural Language | Type naturally (e.g., "500 rupee kharcha kiya") |

### Menu Options
- 💰 Expenses — Track spending
- ⏰ Reminders — Set reminders
- 📅 Habits — Track habits
- ☁️ Weather — Check weather
- 📰 News — Get news
- 📄 DOC Q&A — Upload & query documents

---

## 🔑 Key Implementation Details

### Intent Parsing (Groq AI)
The bot uses Groq's Llama 3.3 model to parse user messages and determine:
- Expense entries
- Reminder requests
- Habit completions
- General queries

### RAG Pipeline
1. User uploads document (PDF/TXT/DOCX)
2. Apache Tika extracts text
3. Text is chunked using TokenTextSplitter
4. Embeddings generated via Ollama (nomic-embed-text)
5. Stored in MongoDB Atlas Vector Store
6. Semantic search answers user questions

### Scheduled Jobs
- **DailySummaryJob** — Sends daily expense summaries
- **ReminderJob** — Processes and sends due reminders

---

## 📊 Database Schema

### Collections
- `expense` — User expense records
- `reminder` — Scheduled reminders
- `habit` / `habitLog` — Habit tracking data
- `user` — User information
- `vector_store` — Document embeddings

---

## 🔒 Security

- Environment variables for sensitive data
- No hardcoded credentials
- Input validation on all user inputs

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

This project is for personal use and learning purposes.

---

## 👨‍💻 Author

Built with ❤️ by Puneet Yadav using Spring Boot and Telegram Bot API

<p align="center">
  <sub>Made with ☕ and 🎵</sub>
</p>

