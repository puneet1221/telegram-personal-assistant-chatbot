# 🤖 Personal Assistant Bot

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-green?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Telegram-Bot-26A5E4?style=for-the-badge&logo=telegram" alt="Telegram">
  <img src="https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb" alt="MongoDB">
  <img src="https://img.shields.io/badge/Groq-AI-orange?style=for-the-badge" alt="Groq AI">
  <img src="https://img.shields.io/badge/RAG-Pipeline-purple?style=for-the-badge" alt="RAG">
</p>

<p align="center">
  <b>An AI-powered Telegram bot that understands natural language — track expenses, set reminders, build habits, and query your documents through simple conversation.</b>
</p>

---

## 📌 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Bot Commands](#-bot-commands)
- [Key Implementation Details](#-key-implementation-details)
- [Database Schema](#-database-schema)
- [Security](#-security)

---

## ✨ Features

### 💰 Expense Management
- Add expenses in natural language — _"aaj 500 khane pe kharch kiye"_
- View, edit, and delete expense records
- Category-wise tracking — food, transport, shopping, health, entertainment
- Monthly spending summaries with category breakdown

### ⏰ Smart Reminders
- One-time reminders — _"kal subah 8 baje gym yaad dilana"_
- Recurring reminders — daily, weekly, monthly, yearly
- Automatic Quartz scheduling — exact time delivery
- Server restart recovery — pending reminders survive restarts

### 📅 Habit Tracking
- Create and manage personal habits
- Daily habit completion logging
- Visual progress tracking — ✅ done / ❌ pending
- Today's habit status at a glance

### 📊 Daily Summary
- Automatic summary every night at 10 PM IST
- Expense breakdown, habit status, tomorrow's reminders — all in one message

### 🌤️ Weather & News
- Real-time weather for any city
- Category-based news — World, Sports, Tech, Business, Health

### 📄 Document Q&A (RAG Pipeline)
- Upload PDF, TXT, DOCX documents
- AI-powered semantic search over your documents
- Ask questions — get context-aware answers
- Session-based isolation — dedicated Q&A mode

### 🔒 Production-Grade Features
- Webhook-based communication — replaces inefficient long polling
- Rate limiting — Bucket4j + Caffeine — 20 requests/minute per user
- Session management — stateful conversation flows
- Multi-user support — data isolated per user via chatId
- Request-scoped AI caching — Groq API called once per message

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Telegram User                      │
└──────────────────────┬──────────────────────────────┘
                       │ HTTPS Webhook
                       ▼
┌─────────────────────────────────────────────────────┐
│            PersonalAssistantBot                      │
│         (TelegramWebhookBot)                         │
│                                                      │
│  Rate Limiter → Session Check → Handler Chain        │
└──────────────────────┬──────────────────────────────┘
                       │
         ┌─────────────┼─────────────┐
         ▼             ▼             ▼
   ┌──────────┐  ┌──────────┐  ┌──────────┐
   │ Handlers │  │ Services │  │ AI Layer │
   │  Chain   │  │  Layer   │  │  Groq+   │
   │ @Order   │  │ Business │  │  RAG     │
   └──────────┘  │  Logic   │  └──────────┘
                 └────┬─────┘
                      │
              ┌───────┴────────┐
              ▼                ▼
        ┌──────────┐    ┌──────────┐
        │ MongoDB  │    │  Quartz  │
        │  Atlas   │    │Scheduler │
        └──────────┘    └──────────┘
```

### Design Patterns Used

| Pattern | Where Used | Why |
|---------|-----------|-----|
| **Chain of Responsibility** | Handler chain with `@Order` | New features without touching existing code |
| **Strategy Pattern** | `MessageHandler` interface | Each handler encapsulates its own logic |
| **Open/Closed Principle** | Handler registration | Open for extension, closed for modification |
| **Request-Scoped Cache** | Groq API responses | One AI call per message — not per handler |
| **Singleton** | Spring beans | Memory efficient |

---

## 🛠️ Tech Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| **Framework** | Spring Boot 3.5 | Core backend framework |
| **Language** | Java 17 | Primary language |
| **Database** | MongoDB Atlas | Data persistence |
| **Bot API** | Telegram Bots 6.8 | Webhook-based messaging |
| **AI Parsing** | Groq API (LLaMA 3.3) | Natural language intent parsing |
| **RAG** | Spring AI + Apache Tika | Document processing & Q&A |
| **Embeddings** | Ollama (nomic-embed-text) | Vector embeddings |
| **Scheduler** | Quartz Scheduler | Precise reminder delivery |
| **Caching** | Caffeine Cache | In-memory request caching |
| **Rate Limiting** | Bucket4j | Per-user request throttling |
| **Build Tool** | Maven | Dependency management |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/project/personal_assistant/
│   │   ├── PersonalAssistantApplication.java
│   │   ├── bot/
│   │   │   ├── PersonalAssistantBot.java        # Central message router
│   │   │   ├── BotConfig.java                   # Webhook registration
│   │   │   ├── WebhookController.java           # Webhook endpoint
│   │   │   ├── DailySummaryJob.java             # 10 PM daily summary
│   │   │   ├── ReminderJob.java                 # Quartz reminder job
│   │   │   └── handler/
│   │   │       ├── MessageHandler.java          # Handler interface (canHandle + handle)
│   │   │       ├── StartHandler.java            # /start command
│   │   │       ├── DoneHandler.java             # /done — session close
│   │   │       ├── ExpenseHandler.java          # AI-parsed expenses
│   │   │       ├── ExpensesListHandler.java     # /expenses list
│   │   │       ├── ExpenseDeleteHandler.java    # delete expense N
│   │   │       ├── ExpenseEditHandler.java      # edit expense N
│   │   │       ├── ReminderHandler.java         # normal + recurring reminders
│   │   │       ├── RemindersListHandler.java    # /reminders list
│   │   │       ├── HabitHandler.java            # habit tracking commands
│   │   │       ├── QnAHandler.java              # /QNA:read-a-file
│   │   │       ├── QnAQuestionHandler.java      # RAG Q&A session
│   │   │       ├── FileUploadHandler.java       # document upload
│   │   │       └── UnknownHandler.java          # fallback
│   │   ├── model/
│   │   │   ├── Expense.java
│   │   │   ├── Reminder.java                    # normal + recurring fields
│   │   │   ├── Habit.java
│   │   │   ├── HabitLog.java
│   │   │   └── User.java
│   │   ├── repo/                                # MongoDB repositories
│   │   ├── service/
│   │   │   ├── GroqChatService.java             # AI intent parsing + Caffeine cache
│   │   │   ├── RAGService.java                  # Document Q&A pipeline
│   │   │   ├── FileProcessorService.java        # Apache Tika extraction
│   │   │   ├── TelegramFileService.java         # File download from Telegram
│   │   │   ├── SessionManagerService.java       # User state management
│   │   │   ├── RateLimiterService.java          # Bucket4j rate limiting
│   │   │   ├── ExpenseService.java
│   │   │   ├── ReminderService.java
│   │   │   ├── HabitService.java
│   │   │   ├── QuartzService.java               # Job scheduling
│   │   │   ├── ReminderRecoveryService.java     # Restart recovery
│   │   │   ├── DailySummaryService.java
│   │   │   └── UserService.java
│   │   └── controllers/
│   │       └── WebhookController.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/project/personal_assistant/
        └── service/
            └── ExpenseServiceTest.java
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB Atlas account (free tier)
- Telegram Bot Token — via [@BotFather](https://t.me/BotFather)
- Groq API Key — [console.groq.com](https://console.groq.com)
- ngrok (for local development)

### Environment Variables

Set these as system environment variables or in your run configuration:

```properties
# Database
dburl=mongodb+srv://<username>:<password>@cluster.mongodb.net/

# Telegram
token=your_telegram_bot_token
botUsername=your_bot_username
webHookUrl=https://your-ngrok-url.ngrok-free.app

# AI APIs
groqApiKey=your_groq_api_key
geminiApiKey=your_gemini_api_key

# External APIs
weatherApiKey=your_openweather_api_key
newsApiKey=your_news_api_key
```

### Build & Run

```bash
# Clone the repository
git clone https://github.com/puneet1221/personal-assistant.git
cd personal-assistant

# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

### Webhook Setup

```bash
# Step 1 — Start ngrok
ngrok http 8080

# Step 2 — Copy the HTTPS URL and set in environment variables
# webHookUrl=https://abc123.ngrok-free.app

# Step 3 — Start the app — webhook auto-registers on startup
./mvnw spring-boot:run
```

---

## 📱 Bot Commands

| Command / Message | Description |
|------------------|-------------|
| `/start` | Show main menu |
| `/done` | Close any active session → back to normal |
| `/expenses` | List all expenses with total |
| `/reminders` | List all reminders |
| `/today` | Today's habit status |
| `/habits` | List all active habits |
| `/add-habit gym` | Add a new habit |
| `/delete-habit 1` | Delete habit by index |
| `/mark-habit-done 1` | Mark habit as completed |
| `/QNA:read-a-file` | Start document Q&A session |
| `aaj 500 khane pe kharch kiye` | Add expense — natural language |
| `kal subah 8 baje gym yaad dilana` | Set reminder — natural language |
| `har din subah 7 baje workout` | Set recurring reminder |
| `delete expense 1` | Delete expense by index |
| `edit expense 1 400 food snacks` | Edit expense by index |

---

## 🔑 Key Implementation Details

### Handler Chain Pattern

Every message goes through an ordered chain of handlers. Each handler implements:

```java
public interface MessageHandler {
    boolean canHandle(String messageText, long chatId); // session-aware
    String handle(Update update, String messageText);
}
```

Adding a new feature = creating a new `@Component` class. Zero changes to existing code.

### AI Intent Parsing (Groq)

User message → Groq LLaMA 3.3 → structured JSON → appropriate handler action

```json
{"type": "expense", "amount": 500, "category": "food", "description": "lunch"}
{"type": "reminder", "datetime": "2026-04-02T08:00:00", "message": "gym"}
{"type": "recurring_reminder", "frequency": "weekly", "day": "MONDAY", "time": "09:00", "message": "meeting"}
```

Caffeine cache ensures **one Groq call per message** — regardless of how many handlers check it.

### RAG Pipeline

```
Upload PDF → Apache Tika extracts text
→ TokenTextSplitter chunks text (2000 tokens, 300 overlap)
→ Ollama generates embeddings (nomic-embed-text)
→ MongoDB Atlas Vector Store stores chunks
→ User asks question → similarity search → Groq generates answer
```

### Reminder System

```
User sets reminder → MongoDB saves → Quartz schedules exact-time job
Server restart → ReminderRecoveryService → reschedules all pending reminders
Recurring reminders → Cron expressions → never marked as sent
```

### Rate Limiting

```
Bucket4j + Caffeine — 20 messages/minute per user
Intervally refill — strict per-minute limit
Auto-expire — inactive users cleaned up after 1 hour
```

### Session Management

```
NORMAL          → all handlers active
WAITING_FOR_FILE → only file upload accepted
QNA_SESSION     → only RAG Q&A handler active
/done           → always resets to NORMAL
```

---

## 📊 Database Schema

| Collection | Key Fields | Purpose |
|-----------|-----------|---------|
| `expense` | chatId, amount, category, date | Expense records |
| `reminder` | chatId, reminderTime, recurring, cronExpression | All reminders |
| `habit` | chatId, name, active | Habit definitions |
| `habit_logs` | chatId, habitId, date | Daily completion logs |
| `user` | chatId | Registered users |
| `vector_store` | embedding, content, metadata | RAG document chunks |

---

## 🔒 Security

- All sensitive keys injected via environment variables — no hardcoded credentials
- Rate limiting — prevents spam and API abuse
- Input validation — all user inputs validated before processing
- Session isolation — each user's data completely separate via chatId
- File validation — only PDF, TXT, DOCX accepted for RAG

---

## 👨‍💻 Author

**Puneet Yadav** — Full Stack Java Developer

- GitHub: [github.com/puneet1221](https://github.com/puneet1221)
- Email: yadavpuneet399@gmail.com

<p align="center">Built with ☕ Java + 🌱 Spring Boot + 🤖 AI</p>