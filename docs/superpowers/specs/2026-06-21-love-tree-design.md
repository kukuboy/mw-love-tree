# LoveTree (爱情树) — Design Spec

**Date:** 2026-06-21  
**Status:** Approved  
**Project:** LoveTree — 情侣空间 Web 应用

---

## 1. Overview

LoveTree 是一个私密的情侣空间 Web 应用。每位用户拥有独立账号，通过邀请码绑定为情侣。核心是一棵随感情成长而变化的虚拟树——每添加一个恋爱事件，树就变得更加繁茂。同时提供时间线、悄悄话、相册、纪念日倒计时等功能。

**视觉风格：** 梦幻手绘风，柔和暖色调，Canvas 绘制的树搭配花瓣/星星飘落动画，营造温暖浪漫的氛围。

---

## 2. Architecture

```
                    Docker Compose 编排
    ┌─────────────────────────────────────────────────────┐
    │                                                     │
    │  ┌──────────┐   ┌──────────┐   ┌───────────────┐   │
    │  │  Nginx   │──▶│ SpringBoot│──▶│    MySQL 8.0  │   │
    │  │  :80     │   │  :8080    │   │    :3306      │   │
    │  │          │   │           │   │               │   │
    │  │ Vue 3 SPA│   │ REST API  │   │ love_tree_db  │   │
    │  │ 静态资源  │   │ MyBatis   │   │               │   │
    │  └──────────┘   └───────────┘   └───────────────┘   │
    │                                                     │
    └─────────────────────────────────────────────────────┘
```

### Tech Stack

| 层 | 技术 | 版本 |
|----|------|------|
| 前端框架 | Vue 3 + Composition API + TypeScript | 3.5+ |
| UI / 动画 | 手写 CSS + Canvas API 粒子动画 | — |
| HTTP 客户端 | Axios | — |
| 路由 | Vue Router 4 | — |
| 状态管理 | Pinia | — |
| 后端框架 | Spring Boot | 3.x |
| ORM | MyBatis + MyBatis-Plus | 3.x |
| 数据库 | MySQL | 8.0 |
| 认证 | JWT (jjwt) | — |
| 文件存储 | 本地磁盘 + 静态资源映射 | — |
| 容器化 | Docker + Docker Compose | — |
| 前端服务器 | Nginx (alpine) | 1.25+ |
| JDK | OpenJDK | 17 |

### Project Structure

```
love-tree/
├── frontend/                # Vue 3 前端项目
│   ├── src/
│   │   ├── api/             # Axios 请求封装
│   │   ├── components/      # 通用组件
│   │   ├── pages/           # 页面组件
│   │   ├── stores/          # Pinia stores
│   │   ├── router/          # Vue Router 配置
│   │   ├── utils/           # 工具函数
│   │   ├── assets/          # 静态资源（图片、字体）
│   │   ├── styles/          # 全局样式 + CSS 变量
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/
│   ├── nginx.conf
│   ├── Dockerfile           # 多阶段构建
│   ├── package.json
│   └── tsconfig.json
├── backend/                 # Spring Boot 后端项目
│   ├── src/
│   │   ├── main/java/com/lovetree/
│   │   │   ├── config/      # Security, CORS, MyBatis 配置
│   │   │   ├── controller/  # REST Controller
│   │   │   ├── service/     # Service 层
│   │   │   ├── mapper/      # MyBatis Mapper
│   │   │   ├── entity/      # 实体类
│   │   │   ├── dto/         # DTO / VO
│   │   │   ├── common/      # 统一响应、异常、常量
│   │   │   └── LoveTreeApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── mapper/      # MyBatis XML
│   │       └── schema.sql   # 建表 SQL
│   ├── pom.xml
│   └── Dockerfile           # 多阶段构建
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 3. Data Model

### 3.1 Entity Relationship

```
┌──────────┐     ┌──────────────┐     ┌──────────┐
│   User   │     │    Couple    │     │   Tree   │
├──────────┤     ├──────────────┤     ├──────────┤
│ id  (PK) │◄───▶│ id      (PK) │◄───▶│ id  (PK) │
│ email    │ 1:2 │ user1_id(FK) │ 1:1 │ couple_id│
│ password │     │ user2_id(FK) │     │ stage    │
│ nickname │     │ invite_code  │     │ leaf_cnt │
│ avatar   │     │ anniversary  │     │ flowers  │
│ created  │     │ created_at   │     └──────────┘
└──────────┘     └──────┬───────┘
       │                │
       ▼                ▼
┌──────────────┐  ┌───────────┐  ┌──────────────┐
│  LoveEvent   │  │  Message  │  │    Photo     │
├──────────────┤  ├───────────┤  ├──────────────┤
│ id      (PK) │  │ id   (PK) │  │ id      (PK) │
│ couple_id(FK)│  │ couple_id │  │ couple_id(FK)│
│ author_id(FK)│  │ from_id   │  │ uploader_id  │
│ title        │  │ to_id     │  │ url          │
│ content      │  │ content   │  │ caption      │
│ event_type   │  │ is_read   │  │ created_at   │
│ event_date   │  │ created   │  └──────────────┘
│ images (JSON)│  └───────────┘
│ mood         │
│ created_at   │
└──────────────┘
```

### 3.2 Tree Growth Stages

| Stage | Label | Event Count |
|-------|-------|-------------|
| 🌱 SEED | 种子 | 0 |
| 🌿 SPROUT | 嫩芽 | 1 – 5 |
| 🪴 SAPLING | 小树 | 6 – 15 |
| 🌸 BLOOM | 花树 | 16 – 30 |
| 🍊 FRUIT | 果树 | 31+ |

### 3.3 Event Types

`first_meet` | `first_date` | `travel` | `anniversary` | `birthday` | `daily` | `other`

Each type maps to a distinct leaf/flower color on the tree:
- first_meet → 粉色花 (pink blossom)
- first_date → 红色玫瑰 (red rose)
- travel → 金色叶 (golden leaf)
- anniversary → 紫色花 (purple flower)
- birthday → 橙色花 (orange flower)
- daily → 绿色叶 (green leaf)
- other → 蓝色叶 (blue leaf)

---

## 4. REST API

### 4.1 Endpoints

**Auth**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |

**Couple**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/couple/invite` | Generate invite code |
| POST | `/api/couple/join` | Join via invite code |
| GET | `/api/couple/info` | Get couple + partner info |

**Tree**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tree` | Get tree state (stage, leaf count, flowers) |
| GET | `/api/tree/stats` | Growth stats (type distribution, monthly heatmap) |

**Events**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/events` | Event list (paginated, newest first) |
| POST | `/api/events` | Create event → tree grows |
| GET | `/api/events/{id}` | Event detail |
| PUT | `/api/events/{id}` | Edit event |
| DELETE | `/api/events/{id}` | Delete event (tree regresses) |

**Messages**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/messages` | Message list with partner |
| POST | `/api/messages` | Send whisper |
| GET | `/api/messages/unread-count` | Unread count |
| PUT | `/api/messages/{id}/read` | Mark as read |

**Photos**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/photos` | Photo list (paginated) |
| POST | `/api/photos` | Upload photo (multipart/form-data) |
| DELETE | `/api/photos/{id}` | Delete photo |

**Special Days**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/special-days` | List of anniversaries + next countdown |

### 4.2 Unified Response Format

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

Error codes: `400` validation, `401` unauthorized, `403` forbidden, `404` not found, `409` conflict, `500` server error.

### 4.3 Authentication

JWT token in `Authorization: Bearer <token>` header. Token expires in 7 days. BCrypt for password hashing.

---

## 5. Frontend Design

### 5.1 Routes

| Path | Page | Auth Required |
|------|------|:---:|
| `/login` | LoginPage | No |
| `/register` | RegisterPage | No |
| `/invite` | InvitePage (pairing) | Yes |
| `/dashboard` | Dashboard — tree, days counter, countdown | Yes |
| `/timeline` | TimelinePage — event waterfall | Yes |
| `/event/new` | EventFormPage | Yes |
| `/event/:id` | EventDetailPage | Yes |
| `/messages` | MessagesPage | Yes |
| `/messages/:userId` | MessageDetailPage | Yes |
| `/album` | AlbumPage | Yes |
| `/settings` | SettingsPage | Yes |

### 5.2 Component Tree (Key Components)

```
App.vue
├── LoginPage, RegisterPage, InvitePage (no layout)
└── AuthenticatedLayout.vue
    ├── AppHeader.vue         (nav + days counter + avatar)
    ├── SideNav.vue           (desktop sidebar / mobile bottom tab)
    │
    ├── DashboardPage.vue
    │   ├── LoveTreeCanvas.vue    ★ Canvas-drawn love tree (soul component)
    │   ├── TreeStageBadge.vue
    │   ├── CountdownCard.vue
    │   └── RecentEvents.vue
    │
    ├── TimelinePage.vue
    │   ├── EventCard.vue
    │   └── EventFilter.vue
    │
    ├── EventFormPage.vue
    │   ├── ImageUploader.vue
    │   ├── MoodPicker.vue        (😊😍🥰😘)
    │   └── DatePicker.vue
    │
    ├── EventDetailPage.vue
    │   └── ImageLightbox.vue
    │
    ├── MessagesPage.vue
    │   ├── MessageBubble.vue
    │   └── MessageInput.vue
    │
    ├── AlbumPage.vue
    │   ├── PhotoCard.vue
    │   └── PhotoUploader.vue
    │
    └── SettingsPage.vue
        ├── AvatarUploader.vue
        ├── CoupleInfo.vue
        └── ProfileForm.vue
```

### 5.3 Visual Design

- **LoveTreeCanvas:** Uses Canvas API to draw a hand-drawn style tree. Renders differently based on `tree.stage`. Leaves/flowers use natural curves, trunk uses warm brown with rough strokes.
- **Background:** Warm gradient (soft pink → cream), CSS-animated floating petals and star particles.
- **Typography:** `"PingFang SC", "Noto Serif SC", serif` — elegant Chinese font stack.
- **Cards:** Frosted glass effect (`backdrop-filter: blur()`), rounded corners (`border-radius: 16px`), soft box shadows.
- **Animations:** Falling petals, rotating flowers, bloom animation on event creation, tree growth transition.
- **Color palette:**
  - Primary: `#E8A0BF` (soft pink)
  - Secondary: `#F3CCD5` (blush)
  - Accent: `#C5898E` (rose)
  - Background: `#FFF5F7` (warm cream)
  - Text: `#4A3B3C` (warm dark)
  - Gold: `#D4A574` (warm gold for accents)

---

## 6. Docker Deployment

### 6.1 Container Architecture

| Service | Image | Port | Volume Mounts |
|---------|-------|------|---------------|
| nginx | Custom (nginx:alpine + Vue dist) | 80:80 | frontend dist (built-in) |
| backend | Custom (eclipse-temurin:17-jre-alpine + jar) | 8080 (internal) | uploads:/app/uploads |
| mysql | mysql:8.0 | 3306 (internal) | mysql_data:/var/lib/mysql |

All services on `love-net` Docker network.

### 6.2 Dockerfiles

**frontend/Dockerfile** — Multi-stage:
- Stage 1: `node:20-alpine` → `npm ci` → `npm run build` → produces `dist/`
- Stage 2: `nginx:alpine` → copy `dist/` + `nginx.conf` → final image ~20MB

**backend/Dockerfile** — Multi-stage:
- Stage 1: `maven:3.9-eclipse-temurin-17` → `mvn package -DskipTests` → produces `.jar`
- Stage 2: `eclipse-temurin:17-jre-alpine` → copy `.jar` → final image ~200MB

### 6.3 docker-compose.yml (Key Points)

- `nginx` depends on `backend`; `backend` depends on `mysql` (with healthcheck)
- Environment variables from `.env` file (passwords, JWT secret)
- MySQL healthcheck: `mysqladmin ping -h localhost`
- Backend init: runs `schema.sql` via MyBatis-Plus auto-DDL or Spring Boot startup hook
- Restart policy: `unless-stopped` for all services

### 6.4 Nginx Configuration

- `/` → SPA static files (`try_files $uri /index.html`)
- `/api/` → reverse proxy to `backend:8080`
- Static assets with long cache (1y), API with no cache
- gzip compression enabled
- CORS headers for API proxy

### 6.5 Startup

```bash
# Copy and configure env
cp .env.example .env
# Edit .env with your secrets

# Build and start all services
docker compose up -d

# View logs
docker compose logs -f
```

---

## 7. Error Handling

### 7.1 Backend

| Scenario | HTTP Code | Behavior |
|----------|-----------|----------|
| Validation failure | 400 | Return field-level error details |
| Unauthenticated / expired token | 401 | Global interceptor, redact to login |
| Forbidden (not this couple's data) | 403 | Reject cross-couple access |
| Resource not found | 404 | Event/message/photo not found |
| Duplicate pairing | 409 | User already belongs to a couple |
| Server error | 500 | `@ControllerAdvice` global handler, logged |

Implementation: Spring `@ControllerAdvice` + `@ExceptionHandler`.

### 7.2 Frontend

Every page handles three states:
- **Loading:** Skeleton screens or spinner
- **Empty:** Illustrated empty state with guiding copy ("还没有一起的记录，去添加第一个吧～")
- **Error:** Friendly error message + retry button

Axios response interceptor for global error toast on 401/500.

### 7.3 Security

- Passwords: BCrypt hashed (Spring Security)
- JWT secret: injected via environment variable only
- SQL injection prevention: MyBatis `#{}` parameter binding (never `${}`)
- File uploads: type whitelist (jpg/png/gif/webp), max 10MB per file
- CORS: allow frontend origin only
- All couple-scoped data verified against current user's couple_id

---

## 8. Testing Strategy

| Scope | Tool | What |
|-------|------|------|
| Backend unit | JUnit 5 + Mockito | Service layer logic |
| Backend DAO | MyBatis Test + H2 in-memory | SQL mapping correctness |
| Backend API | MockMvc | Controller layer integration |
| Frontend unit | Vitest | Component logic, Pinia stores |
| Frontend E2E | Playwright | Critical flows: register → pair → add event → view tree |

---

## 9. README.md Outline

The README.md must be comprehensive and include:

1. **Project title** with description
2. **Screenshots** (placeholder — add after deployment)
3. **Tech stack** badges/table
4. **Features list**
5. **Project structure** diagram
6. **Prerequisites** (Docker, Docker Compose)
7. **Quick start** (clone, cp .env, docker compose up -d)
8. **Environment variables** table
9. **API documentation** (endpoint summary)
10. **Development guide** (how to run frontend/backend locally without Docker)
11. **Deployment guide** (Docker details)
12. **Architecture diagram** (ASCII)
13. **Database schema**
14. **License**

---
