# 📰 Blog Gestion – JavaFX Backend Module

## 📌 Project Overview

**Blog Gestion** is a JavaFX-based backend module developed as part of the **GestionCoach** application.  
It enables administrators to efficiently manage blog articles with advanced intelligent features such as content quality analysis and trending scoring.  

The module integrates seamlessly into the **Admin backend interface** without impacting other modules or project functionality.

---

## 🎯 Key Features

### ✅ Core Functionality
- Full CRUD operations (Create, Read, Update, Delete)
- Advanced search by title or content
- Sorting and filtering of articles
- PDF export of articles table
- Real-time statistics generation
- Clean and intuitive Admin dashboard integration

### 🤖 Intelligent Features
- **AI Content Quality Analyzer**
  - Evaluates article quality based on length, structure, and keyword usage
  - Provides actionable quality feedback
- **Trending Algorithm**
  - Computes trending scores based on engagement logic
  - Identifies and highlights popular articles

---

## 🏗 Architecture & Structure

The module follows the existing project architecture and is fully compatible with the main application:

```

edu.Connexion3A7
├── Controller
├── Services
├── Entities
├── Analyzers
├── Tools

```

### Integration Notes:
- Reuses the main project’s database connection
- Preserves Admin dashboard navigation
- Maintains existing JavaFX project structure
- No modifications to other modules were required

---

## 🗄 Database

The Blog Gestion module uses the existing **GestionCoach** database.

- **Table: `blog`**
  - Fields: `id`, `title`, `content`, `date`, etc.
- No additional tables or modifications were made to other tables.

---

## 📂 Technology Stack

- **Java 17**
- **JavaFX**
- **Maven**
- **MySQL**
- **iText** (PDF generation)

---

## 🔐 Access & Usage

Accessible through:

```

Admin → Backend → Blog Gestion

```

> Note: This module is currently **admin-only**. It does not expose any frontend features to users.

---

## 🚀 Integration Strategy

1. Developed as a **standalone module** first.
2. Seamlessly integrated into the GestionCoach Admin backend.
3. Reused existing database connections and project structure.
4. Preserved all other system functionalities (Users, Formations, etc.).
5. No structural refactoring or modifications to other modules were performed.

---

## 👨‍💻 Author

**Hamza Snoussi**  
Backend Developer – Blog Gestion Module

---

## 🔮 Future Enhancements

- User-facing blog frontend
- Commenting and interaction system
- Engagement-driven trending calculations
- External AI API integration for richer content analysis
- Role-based permissions for enhanced management

---

## 📌 Additional Notes

- Project is **non-modular** (no `module-info.java` present)
- No refactoring of other modules was required
- All existing functionalities of **GestionCoach** remain fully intact

---

## 💎 Optional Enhancements for README

- Minimalistic version for quick overview
- Technical version for developer-focused documentation
- Badge-enhanced version with Java, Maven, MySQL icons
- Academic/school presentation style

```

---


