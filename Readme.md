# Personal Assistant Bot

Day1: 

**User → Telegram → `onUpdateReceived()` → `handleExpense()` → `ExpenseService` → `ExpenseRepository` → MongoDB Atlas → reply wapas phone pe**

<<<<<<< HEAD
=======
**Issue: Agar server band ho jaye to reminders pending rahenge, user ko nahi milenge. sare reminders pending rahenge, user ko nahi milenge. qki scheduling in memory hoti hai, server restart pe sab khatam ho jata hai.**

Soltuion: 

persitent-quartz:Server start hota hai
→ MongoDB se saare pending reminders lo (sent=false)
→ Jo future mein hain unhe Quartz mein dobara schedule karo
→ Done — server restart pe bhi reminders survive karenge

2 cases handle kiye:
Future reminders — Quartz mein dobara schedule karo — user ko milenge.
Expired reminders — Server band tha jab reminder fire hona tha tab nhi hue — ab sent=true mark kiya to indicate that they are expired. User won't get them, but at least they won't be pending forever.

>>>>>>> feature/quartz-persistent
