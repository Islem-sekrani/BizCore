# BizCore — Coaching Management Platform

A full-stack **Symfony 6.4** web application for managing coaches, events, articles, and users. Built for the ESPRIT PI-DEV project (3A7 — 2026).

![PHP](https://img.shields.io/badge/PHP-≥8.1-777BB4?logo=php&logoColor=white)
![Symfony](https://img.shields.io/badge/Symfony-6.4-000000?logo=symfony&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.4-003545?logo=mariadb&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green)

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Default Accounts](#default-accounts)
- [Project Structure](#project-structure)
- [Entities & Database Schema](#entities--database-schema)
- [Routes](#routes)
- [Services](#services)
- [Security](#security)
- [Configuration](#configuration)
- [Screenshots](#screenshots)
- [Team](#team)

---

## Features

| Module | Description |
|---|---|
| **Coach Management** | Full CRUD, search/filter by domain, tariff & availability, duplicate detection, CSV export |
| **Coach Booking** | Weekly availability calendar, real-time reservation API, conflict detection, cancellation |
| **Coach Rating & Reviews** | 1–5 star ratings with comments, average rating sync, unique constraint per user+coach |
| **Coach Statistics** | 4-KPI scoring (occupation rate, weighted rating, client loyalty, trend), ranked leaderboard |
| **Event Management** | CRUD with image upload, public event browsing, event registration with ticket count |
| **Blog / Articles** | CRUD with image upload, public blog view, view counter, categories |
| **User Management** | Admin CRUD from dashboard, role assignment, paginated search, sorting |
| **AI Chatbot** | Coaching assistant powered by HuggingFace LLM, session-based conversation, DB-aware recommendations |
| **Notifications** | In-app notification bell + SMS alerts via Twilio |
| **Dashboard** | Central admin hub with KPI cards, user management, calendar, AJAX partial loading |
| **PDF Reports** | Dashboard statistics exported as PDF via DomPDF |
| **Phone Detection** | IP-based country detection for phone input formatting (32 countries) |
| **Authentication** | Form login, registration, password hashing, role-based access control |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | PHP 8.1+, Symfony 6.4 |
| **ORM** | Doctrine ORM 3.6 |
| **Database** | MariaDB 10.4 (XAMPP) |
| **Templating** | Twig 3 |
| **PDF Generation** | DomPDF 3.1 |
| **Pagination** | KnpPaginatorBundle 6.10 |
| **SMS** | Twilio REST API |
| **AI** | HuggingFace Inference API |
| **Frontend** | HTML5, CSS3, JavaScript (vanilla), Font Awesome, Bootstrap 5 |

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                      Browser                             │
├──────────────────────────────────────────────────────────┤
│   Symfony Router → Controller → Service → Repository     │
│                         ↓                                │
│                   Twig Templates                         │
│                         ↓                                │
│               Doctrine ORM → MariaDB                     │
├──────────────────────────────────────────────────────────┤
│   External APIs: Twilio SMS │ HuggingFace AI │ ipapi.co  │
└──────────────────────────────────────────────────────────┘
```

---

## Prerequisites

- **PHP** ≥ 8.1 with extensions: `pdo_mysql`, `intl`, `mbstring`, `xml`, `curl`
- **Composer** 2.x
- **XAMPP** (Apache + MariaDB 10.4) or equivalent MySQL 5.7+
- **Git**

---

## Installation

```bash
# 1. Clone the repository
git clone https://github.com/oumayma2004/ESPRIT-PIDEV-WEB-3A7-2026-BIZCORE.git
cd ESPRIT-PIDEV-WEB-3A7-2026-BIZCORE

# 2. Install PHP dependencies
composer install

# 3. Copy and configure environment
cp .env .env.local
# Edit .env.local with your database credentials and API keys
```

---

## Database Setup

```bash
# 1. Create the database (via XAMPP phpMyAdmin or CLI)
# Import the SQL dump:
mysql -u root bizcore < bizcore.sql

# 2. Or use Doctrine migrations:
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate
```

The database `bizcore` contains 12 tables with pre-seeded data including 22 coaches, roles, and sample users.

---

## Running the Application

```bash
# Option 1: Symfony CLI
symfony serve

# Option 2: PHP built-in server
php -S localhost:8000 -t public

# Option 3: XAMPP Apache (configure DocumentRoot to public/)
```

Visit: **http://localhost:8000**

---

## Default Accounts

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@bizcore.tn` | `test123` |
| **User** | `user@bizcore.tn` | `test123` |

---

## Project Structure

```
├── config/
│   ├── packages/
│   │   ├── security.yaml          # Auth & access control
│   │   ├── doctrine.yaml          # ORM configuration
│   │   └── ...
│   ├── routes.yaml                # Route imports
│   └── services.yaml              # Service wiring & env params
│
├── src/
│   ├── Controller/
│   │   ├── SecurityController.php       # Login, register, logout
│   │   ├── DashboardController.php      # Admin dashboard + PDF report
│   │   ├── UserController.php           # User CRUD (admin)
│   │   ├── ProfileController.php        # Self-service profile
│   │   ├── CoachController.php          # Coach front-office
│   │   ├── ReservationController.php    # Coach booking JSON API
│   │   ├── RatingController.php         # Coach rating JSON API
│   │   ├── ChatbotController.php        # AI chatbot JSON API
│   │   ├── ArticleController.php        # Article CRUD (admin)
│   │   ├── FrontendBlogController.php   # Public blog
│   │   ├── EvenementController.php      # Event CRUD (admin)
│   │   ├── EvenementFrontController.php # Public events
│   │   ├── ReservationEvenementController.php  # Event reservations
│   │   ├── HomeController.php           # Landing page
│   │   └── Admin/
│   │       ├── AdminCoachController.php           # Admin coach panel
│   │       └── AdminCoachStatisticsController.php # Coach statistics
│   │
│   ├── Entity/                    # 12 Doctrine entities
│   ├── Repository/                # 12 Doctrine repositories
│   ├── Service/
│   │   ├── ChatbotService.php              # AI chatbot logic
│   │   ├── CoachService.php                # Coach business logic
│   │   ├── CoachStatisticsService.php      # 4-KPI scoring engine
│   │   ├── CsvExportService.php            # CSV export
│   │   ├── NotificationService.php         # In-app notifications
│   │   ├── PhoneCountryDetectionService.php # IP geolocation
│   │   ├── ReservationService.php          # Booking logic + SMS
│   │   └── TwilioSmsService.php            # SMS via Twilio
│   └── Form/                      # 6 Symfony form types
│
├── templates/
│   ├── base.html.twig             # Main layout (sidebar + topbar)
│   ├── admin/coach/               # Admin coach templates
│   ├── admin/statistics/          # Statistics dashboard
│   ├── article/                   # Article CRUD templates
│   ├── blog/                      # Public blog
│   ├── coach/                     # Coach front-office templates
│   ├── components/                # Reusable popups & notification bell
│   ├── dashboard/                 # Dashboard + AJAX partials + PDF
│   ├── evenement/                 # Event admin templates
│   ├── events/                    # Public event pages
│   ├── home/                      # Landing page
│   ├── profile/                   # User profile
│   ├── reservation/               # Event reservation templates
│   └── security/                  # Login & register
│
├── migrations/                    # Doctrine migrations
├── public/index.php               # Front controller
├── app/                           # Legacy MVC layer (pre-Symfony)
├── bizcore.sql                    # Database dump
├── composer.json
└── compose.yaml                   # Docker Compose (optional)
```

---

## Entities & Database Schema

```
┌──────────┐     ┌──────────────┐     ┌───────────┐
│   Role   │────<│     User     │>────│  Notification │
└──────────┘     └──────┬───────┘     └───────────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
    ┌─────▼─────┐ ┌─────▼─────┐ ┌─────▼──────────────┐
    │ Reservation│ │  Rating   │ │InscriptionEvenement│
    └─────┬─────┘ └─────┬─────┘ └─────┬──────────────┘
          │             │             │
    ┌─────▼─────┐       │       ┌─────▼─────┐
    │   Coach   │◄──────┘       │ Evenement │
    └─────┬─────┘               └───────────┘
          │
    ┌─────▼──────────┐
    │ Disponibilite  │
    └────────────────┘

    ┌──────────┐  ┌──────────┐
    │ Article  │  │ Produit  │  (standalone)
    └──────────┘  └──────────┘
    ┌──────────┐
    │ Commande │  (standalone)
    └──────────┘
```

| Entity | Table | Key Columns |
|---|---|---|
| **User** | `users` | id_user, nom, prenom, email, password, telephone, adresse, statut, id_role (FK→role) |
| **Role** | `role` | id_role, nom_role, description |
| **Coach** | `coach` | id_coach, nom, prenom, biographie, experience_annees, tarif_horaire, disponibilite, domaine, num_tel, note_moyenne |
| **Reservation** | `reservation` | id_reservation, id_user (FK→users), id_coach (FK→coach), date_reservation, date_seance, statut |
| **Disponibilite** | `disponibilite` | id_dispo, id_coach (FK→coach), jour, statut |
| **Rating** | `rating` | id_rating, id_user (FK→users), id_coach (FK→coach), note (1-5), commentaire |
| **Notification** | `notification` | id_notification, id_user (FK→users), message, type, is_read |
| **Article** | `article` | id_article, titre, contenu, image_principale, categorie, statut, nombre_vues |
| **Evenement** | `evenement` | id_evenement, titre, description, prix, date_debut, capacite_max, image_url |
| **InscriptionEvenement** | `inscription_evenement` | id_inscription, id_evenement (FK), id_user (FK), nom, email, nombre_places, statut |
| **Produit** | `produit` | id_produit, nom_produit, description, prix, stock_disponible, categorie |
| **Commande** | `commande` | id_commande, date_commande, montant_total, statut, mode_paiement |

---

## Routes

### Authentication
| Route | Method | Path | Description |
|---|---|---|---|
| `app_login` | ANY | `/auth/login` | Login page |
| `app_register` | GET\|POST | `/auth/register` | Registration page |
| `app_logout` | ANY | `/auth/logout` | Logout |

### Dashboard & Users (Admin)
| Route | Method | Path | Description |
|---|---|---|---|
| `dashboard` | GET | `/dashboard` | Admin dashboard |
| `dashboard_load` | GET | `/dashboard/load` | AJAX partial loader |
| `dashboard_report` | GET | `/dashboard/report` | PDF export |
| `user_store` | POST | `/user/store` | Create user |
| `user_update` | POST | `/user/update` | Update user |
| `user_delete` | GET | `/user/delete/{id}` | Delete user |

### Coach (Front-Office)
| Route | Method | Path | Description |
|---|---|---|---|
| `app_coach_index` | GET | `/coach/` | List coaches |
| `app_coach_search` | GET | `/coach/search` | Search coaches |
| `app_coach_new` | GET\|POST | `/coach/new` | Create coach |
| `app_coach_show` | GET | `/coach/{id}` | View coach |
| `app_coach_edit` | GET\|POST | `/coach/{id}/edit` | Edit coach |
| `app_coach_delete` | GET\|POST | `/coach/{id}/delete` | Delete coach |

### Coach (Admin)
| Route | Method | Path | Description |
|---|---|---|---|
| `admin_coach_index` | GET | `/admin/coach/` | Admin coach list |
| `admin_coach_new` | GET\|POST | `/admin/coach/nouveau` | Create coach |
| `admin_coach_show` | GET | `/admin/coach/{id}` | View coach |
| `admin_coach_edit` | GET\|POST | `/admin/coach/{id}/modifier` | Edit coach |
| `admin_coach_delete` | POST | `/admin/coach/{id}/supprimer` | Delete coach |
| `admin_coach_export_csv` | GET | `/admin/coach/export-csv` | CSV export |
| `admin_statistics_index` | GET | `/admin/statistiques/` | Statistics dashboard |
| `admin_statistics_detailed` | GET | `/admin/statistiques/detailles` | Detailed stats |

### Coach Booking (JSON API)
| Route | Method | Path | Description |
|---|---|---|---|
| `api_coach_availability` | GET | `/reservation/api/coach/{id}/availability` | Get availability |
| `api_coach_set_availability` | POST | `/reservation/api/coach/{id}/availability` | Set availability |
| `api_create_reservation` | POST | `/reservation/api/reserve` | Book session |
| `api_cancel_reservation` | POST | `/reservation/api/cancel/{id}` | Cancel booking |
| `api_coach_reservations` | GET | `/reservation/api/coach/{id}/reservations` | List bookings |

### Rating (JSON API)
| Route | Method | Path | Description |
|---|---|---|---|
| `api_coach_rating_stats` | GET | `/api/rating/coach/{id}/stats` | Rating stats |
| `api_coach_rate` | POST | `/api/rating/coach/{id}/rate` | Submit rating |
| `api_coach_ratings_list` | GET | `/api/rating/coach/{id}/all` | All ratings |

### Events
| Route | Method | Path | Description |
|---|---|---|---|
| `evenement_index` | GET | `/evenement/` | Admin event list |
| `evenement_new` | GET\|POST | `/evenement/new` | Create event |
| `evenement_edit` | GET\|POST | `/evenement/{id}/edit` | Edit event |
| `evenement_delete` | POST | `/evenement/{id}/delete` | Delete event |
| `evenement_front` | GET | `/events` | Public events |
| `evenement_show` | GET | `/events/{id}` | Event details |

### Blog
| Route | Method | Path | Description |
|---|---|---|---|
| `article_index` | GET | `/article/` | Admin article list |
| `article_new` | GET\|POST | `/article/new` | Create article |
| `article_edit` | GET\|POST | `/article/{id}/edit` | Edit article |
| `article_delete` | POST | `/article/{id}/delete` | Delete article |
| `frontend_blog_index` | GET | `/blog` | Public blog |
| `frontend_blog_show` | GET | `/blog/{id}` | Read article |

### Chatbot & Notifications (JSON API)
| Route | Method | Path | Description |
|---|---|---|---|
| `chatbot_message` | POST | `/chatbot/message` | Send AI message |
| `chatbot_clear` | POST | `/chatbot/clear` | Clear history |
| `api_notifications_list` | GET | `/reservation/api/notifications` | Get notifications |
| `api_notification_read` | POST | `/reservation/api/notifications/{id}/read` | Mark read |
| `api_notifications_read_all` | POST | `/reservation/api/notifications/read-all` | Mark all read |

---

## Services

| Service | Description |
|---|---|
| `ChatbotService` | AI coaching assistant via HuggingFace API. Maintains session history (max 20 messages), builds context from coach database. |
| `CoachService` | Coach CRUD business logic: save with duplicate detection, search/filter, domain statistics, global average rating. |
| `CoachStatisticsService` | 4-KPI scoring engine — Occupation Rate (35%), Weighted Rating (35%), Client Loyalty (20%), Trend (10%). Produces ranked leaderboard. |
| `CsvExportService` | Streams all coaches as a UTF-8 CSV (semicolon-delimited). |
| `NotificationService` | In-app notifications: create, mark read, bulk mark-all-read, unread count. |
| `PhoneCountryDetectionService` | IP geolocation via ipapi.co → phone calling code + format for 32 countries. Default: Tunisia (+216). |
| `ReservationService` | Full booking flow: validates coach, checks availability, prevents double-booking, persists, notifies, sends SMS. |
| `TwilioSmsService` | SMS delivery via Twilio REST API. Gracefully skips when credentials are placeholders. |

---

## Security

### Roles
| Role | Access |
|---|---|
| `PUBLIC_ACCESS` | `/auth/login`, `/auth/register`, `/auth/logout`, `/blog`, `/` |
| `ROLE_USER` | All other routes (requires authentication) |
| `ROLE_ADMIN` | Dashboard, user management, admin coach panel, admin event/article views |

### How roles work
- Roles are stored in the `role` database table (`id_role=1` → Admin, `id_role=2` → User)
- The `User` entity has a `ManyToOne` relationship to `Role` with `EAGER` fetch
- `getRoles()` derives Symfony roles from the database role entity

### CSRF Protection
- Login form: `enable_csrf: true` in security.yaml
- Delete operations: CSRF token validation via `isCsrfTokenValid()`

---

## Configuration

### Environment Variables (`.env.local`)

```env
# Database
DATABASE_URL="mysql://root:@127.0.0.1:3306/bizcore"

# Twilio SMS (optional)
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
TWILIO_FROM_NUMBER=+1234567890
TWILIO_TO_NUMBER=+21600000000

# AI Chatbot (optional)
AI_API_URL=https://api-inference.huggingface.co/models/your-model
AI_API_KEY=hf_your_key
AI_MODEL=your-model-name
```

---

## Team

**ESPRIT PI-DEV — 3A7 — 2026 — BizCore**

| Member | Module |
|---|---|
| Islem | Coaching (Coach CRUD, Booking, Rating, Statistics, Chatbot, SMS, CSV) |
| Teammates | Events, Blog/Articles, User Management, Products, Orders |

---

## License

This project is developed for educational purposes as part of the ESPRIT PI-DEV program.
