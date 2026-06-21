# 🌸 LoveTree (爱情树)

属于两个人的私密空间，用一棵树记录恋爱的点点滴滴。

<!-- TODO: add screenshots after deployment -->

---

## Tech Stack

| Category  | Technology                               |
|-----------|------------------------------------------|
| Frontend  | Vue 3, TypeScript, Pinia, Vue Router 4, Canvas API |
| Backend   | Spring Boot 3, MyBatis-Plus, JWT (jjwt) |
| Database  | MySQL 8.0                                |
| DevOps    | Docker, Docker Compose, Nginx            |

---

## Features

- **🌳 可成长的爱情树** — 每记录一个恋爱事件，树就成长一次。5个阶段：种子 (SEED) → 嫩芽 (SPROUT) → 小树 (SAPLING) → 花树 (BLOOM) → 果树 (FRUIT)
- **📜 恋爱事件时间线** — 按时间倒序排列属于两人的回忆，支持按事件类型筛选
- **💌 情侣悄悄话** — 私密聊天，只有你和 TA 能看见
- **🖼️ 共享相册** — 共同上传照片，拼凑属于两人的相册
- **⏳ 纪念日倒计时** — 在一起的天数 + 下一个纪念日倒计时
- **👫 独立账号 + 邀请配对** — 各自拥有独立账号，通过邀请码绑定为情侣
- **🎨 梦幻手绘视觉风格** — Canvas 绘制爱情树，柔和暖色调，搭配花瓣/星星飘落动画

---

## Project Structure

```
love-tree/
├── frontend/                          # Vue 3 前端项目
│   ├── src/
│   │   ├── api/                       # Axios 请求封装
│   │   │   ├── auth.ts                # 认证相关 API
│   │   │   ├── client.ts              # Axios 实例 + 拦截器
│   │   │   ├── couple.ts              # 情侣配对 API
│   │   │   ├── events.ts              # 事件 API
│   │   │   ├── messages.ts            # 消息 API
│   │   │   ├── photos.ts              # 照片 API
│   │   │   ├── specialDays.ts         # 纪念日 API
│   │   │   ├── tree.ts                # 树状态 API
│   │   │   └── index.ts               # API 统一导出
│   │   ├── components/                # 通用组件
│   │   │   ├── AppHeader.vue          # 顶部导航栏
│   │   │   ├── AvatarUploader.vue     # 头像上传
│   │   │   ├── CountdownCard.vue      # 纪念日倒计时卡片
│   │   │   ├── CoupleInfo.vue         # 情侣信息展示
│   │   │   ├── EventCard.vue          # 事件卡片
│   │   │   ├── EventFilter.vue        # 事件类型筛选
│   │   │   ├── ImageLightbox.vue      # 图片灯箱预览
│   │   │   ├── ImageUploader.vue      # 图片上传
│   │   │   ├── LoveTreeCanvas.vue     # ★ Canvas 绘制的爱情树
│   │   │   ├── MessageBubble.vue      # 聊天气泡
│   │   │   ├── MessageInput.vue       # 聊天输入框
│   │   │   ├── MoodPicker.vue         # 心情选择器
│   │   │   ├── PhotoCard.vue          # 照片卡片
│   │   │   ├── PhotoUploader.vue      # 照片上传
│   │   │   ├── RecentEvents.vue       # 最近事件列表
│   │   │   ├── SideNav.vue            # 侧边导航栏
│   │   │   └── TreeStageBadge.vue     # 树阶段徽章
│   │   ├── pages/                     # 页面组件
│   │   │   ├── LoginPage.vue          # 登录页
│   │   │   ├── RegisterPage.vue       # 注册页
│   │   │   ├── InvitePage.vue         # 绑定伴侣页
│   │   │   ├── DashboardPage.vue      # 仪表盘（主页）
│   │   │   ├── TimelinePage.vue       # 事件时间线
│   │   │   ├── EventFormPage.vue      # 创建/编辑事件
│   │   │   ├── EventDetailPage.vue    # 事件详情
│   │   │   ├── MessagesPage.vue       # 悄悄话列表
│   │   │   ├── MessageDetailPage.vue  # 与 TA 的聊天
│   │   │   ├── AlbumPage.vue          # 共享相册
│   │   │   └── SettingsPage.vue       # 个人设置
│   │   ├── stores/                    # Pinia 状态管理
│   │   │   ├── auth.ts                # 认证状态
│   │   │   ├── couple.ts              # 情侣信息
│   │   │   └── tree.ts                # 树状态
│   │   ├── router/
│   │   │   └── index.ts               # Vue Router 配置
│   │   ├── styles/
│   │   │   ├── animations.css         # 飘落花瓣/星星动画
│   │   │   ├── global.css             # 全局样式
│   │   │   └── variables.css          # CSS 变量（颜色/圆角/阴影）
│   │   ├── types/
│   │   │   └── index.ts               # TypeScript 类型定义
│   │   ├── App.vue                    # 根组件
│   │   ├── main.ts                    # 入口文件
│   │   └── env.d.ts                   # 环境类型声明
│   ├── nginx.conf                     # Nginx 配置（SPA + API 代理）
│   ├── Dockerfile                     # 多阶段构建（node build → nginx)
│   ├── vite.config.ts                 # Vite 构建配置
│   ├── tsconfig.json                  # TypeScript 配置
│   └── package.json                   # 前端依赖
│
├── backend/                           # Spring Boot 后端项目
│   ├── src/main/java/com/lovetree/
│   │   ├── config/                    # 安全 & 配置
│   │   │   ├── CurrentUser.java       # 当前用户注解
│   │   │   ├── CurrentUserResolver.java
│   │   │   ├── JwtInterceptor.java    # JWT 拦截器
│   │   │   └── WebConfig.java         # CORS / 拦截器注册
│   │   ├── controller/                # RESTful 控制器
│   │   │   ├── AuthController.java    # 认证（注册/登录）
│   │   │   ├── CoupleController.java  # 情侣配对
│   │   │   ├── TreeController.java    # 树状态 & 统计
│   │   │   ├── EventController.java   # 事件 CRUD
│   │   │   ├── MessageController.java # 悄悄话
│   │   │   ├── PhotoController.java   # 照片管理
│   │   │   └── SpecialDayController.java
│   │   ├── service/                   # 业务逻辑接口
│   │   │   ├── impl/                  # 实现类
│   │   │   └── ...
│   │   ├── mapper/                    # MyBatis Mapper
│   │   ├── entity/                    # 实体类
│   │   │   ├── User.java
│   │   │   ├── Couple.java
│   │   │   ├── Tree.java
│   │   │   ├── LoveEvent.java
│   │   │   ├── Message.java
│   │   │   └── Photo.java
│   │   ├── dto/                       # 数据传输对象
│   │   ├── common/                    # 通用基础设施
│   │   │   ├── ApiResponse.java       # 统一响应封装
│   │   │   ├── BusinessException.java # 业务异常
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── JwtUtil.java           # JWT 工具类
│   │   └── LoveTreeApplication.java   # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml            # 应用配置
│   │   ├── mapper/                    # MyBatis XML
│   │   └── schema.sql                 # 建表 SQL
│   ├── pom.xml                        # Maven 依赖
│   ├── Dockerfile                     # 多阶段构建（Maven build → JRE）
│   └── .dockerignore
│
├── docker-compose.yml                 # Docker Compose 编排
├── .env.example                       # 环境变量模板
├── .gitignore
└── README.md
```

---

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) (version 24+)
- [Docker Compose](https://docs.docker.com/compose/install/) (v2, bundled with Docker Desktop)

For local development (without Docker), additionally:
- [Node.js](https://nodejs.org/) 20+ & npm
- [JDK](https://adoptium.net/) 17+ & [Maven](https://maven.apache.org/) 3.9+
- MySQL 8.0

---

## Quick Start

```bash
# 1. Clone the repository
git clone <repo-url>
cd love-tree

# 2. Configure environment variables
cp .env.example .env
# Edit .env with your secrets (mysql passwords, JWT secret)

# 3. Build and start all services
docker compose up -d

# 4. Open in browser
open http://localhost
```

---

## Environment Variables

| Variable                 | Description                          | Default                    |
|--------------------------|--------------------------------------|----------------------------|
| `MYSQL_ROOT_PASSWORD`    | MySQL root password                  | *(required)*               |
| `MYSQL_USER`             | Application database user            | `lovetree`                 |
| `MYSQL_PASSWORD`         | Application database user password   | *(required)*               |
| `JWT_SECRET`             | JWT signing secret (256+ bits)       | *(required)*               |

---

## API Documentation

All endpoints return a unified JSON response:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

Error codes: `400` Validation, `401` Unauthorized, `403` Forbidden, `404` Not Found, `409` Conflict, `500` Server Error.

Authentication: `Authorization: Bearer <token>` header. Token expires in 7 days.

### Auth

| Method | Path                   | Auth | Description                         |
|--------|------------------------|------|-------------------------------------|
| POST   | `/api/auth/register`   | No   | Register a new user                 |
| POST   | `/api/auth/login`      | No   | Login, returns JWT token            |

### Couple

| Method | Path                   | Auth | Description                         |
|--------|------------------------|------|-------------------------------------|
| POST   | `/api/couple/invite`   | Yes  | Generate invite code for pairing    |
| POST   | `/api/couple/join`     | Yes  | Join a couple via invite code       |
| GET    | `/api/couple/info`     | Yes  | Get couple info & partner details   |

### Tree

| Method | Path                   | Auth | Description                         |
|--------|------------------------|------|-------------------------------------|
| GET    | `/api/tree`            | Yes  | Get tree state (stage, leaf/flower counts) |
| GET    | `/api/tree/stats`      | Yes  | Growth statistics (type distribution, heatmap) |

### Events

| Method | Path                   | Auth | Description                         |
|--------|------------------------|------|-------------------------------------|
| GET    | `/api/events`          | Yes  | List events (paginated, newest first) |
| POST   | `/api/events`          | Yes  | Create event (tree grows)           |
| GET    | `/api/events/{id}`     | Yes  | Get event detail                    |
| PUT    | `/api/events/{id}`     | Yes  | Update event                        |
| DELETE | `/api/events/{id}`     | Yes  | Delete event (tree regresses)       |

### Messages

| Method | Path                          | Auth | Description                         |
|--------|-------------------------------|------|-------------------------------------|
| GET    | `/api/messages`               | Yes  | List messages with partner          |
| POST   | `/api/messages`               | Yes  | Send a whisper                      |
| GET    | `/api/messages/unread-count`  | Yes  | Get unread message count            |
| PUT    | `/api/messages/{id}/read`     | Yes  | Mark message as read                |

### Photos

| Method | Path                   | Auth | Description                         |
|--------|------------------------|------|-------------------------------------|
| GET    | `/api/photos`          | Yes  | List photos (paginated)             |
| POST   | `/api/photos`          | Yes  | Upload photo (multipart/form-data)  |
| DELETE | `/api/photos/{id}`     | Yes  | Delete photo                        |

### Special Days

| Method | Path                     | Auth | Description                         |
|--------|--------------------------|------|-------------------------------------|
| GET    | `/api/special-days`      | Yes  | List anniversaries + next countdown |

---

## Development Guide

### Frontend (Vue 3)

```bash
cd frontend
npm install
npm run dev
```

The dev server runs at `http://localhost:5173` with hot module replacement. It expects the backend at `http://localhost:8080` (configured in `src/api/client.ts`).

Build for production:

```bash
npm run build
```

Output is written to `frontend/dist/`.

### Backend (Spring Boot)

Set up a local MySQL 8.0 database (or use the Docker Compose mysql service only):

```bash
# Start only MySQL via Docker (optional, but convenient)
docker compose up -d mysql
```

Then start the backend:

```bash
cd backend
mvn spring-boot:run
```

The backend starts at `http://localhost:8080`. The database schema is initialized automatically via `schema.sql` on startup.

### Database (Local MySQL)

If running MySQL locally instead of Docker:

```sql
CREATE DATABASE love_tree CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lovetree'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON love_tree.* TO 'lovetree'@'%';
FLUSH PRIVILEGES;
```

Update `backend/src/main/resources/application.yml` or set environment variables accordingly.

---

## Deployment Guide

The application runs as three Docker containers orchestrated by Docker Compose:

### Container Architecture

```
┌─────────────┐     ┌──────────────┐     ┌───────────────┐
│   Nginx      │────▶│  Spring Boot │────▶│   MySQL 8.0   │
│   :80        │     │  :8080       │     │   :3306       │
│              │     │              │     │               │
│  Vue 3 SPA   │     │  REST API    │     │  love_tree    │
│  静态资源     │     │  MyBatis     │     │  数据库        │
└─────────────┘     └──────────────┘     └───────────────┘
```

All services communicate over the `love-net` Docker bridge network.

### Dockerfiles

**frontend/Dockerfile** (multi-stage):
- Stage 1 (build): `node:20-alpine` — `npm ci` → `npm run build` → produces `dist/`
- Stage 2 (run): `nginx:alpine` — copies `dist/` and `nginx.conf` → serves static files and proxies API requests

**backend/Dockerfile** (multi-stage):
- Stage 1 (build): `maven:3.9-eclipse-temurin-17` — `mvn package -DskipTests` → produces `.jar`
- Stage 2 (run): `eclipse-temurin:17-jre-alpine` — copies `.jar` and runs it

### Volumes

| Volume         | Container Mount     | Purpose                        |
|----------------|---------------------|--------------------------------|
| `mysql_data`   | `/var/lib/mysql`    | Persistent MySQL data storage  |
| `uploads`      | `/app/uploads`      | User-uploaded photos & avatars |

### Network Topology

```
                    love-net (bridge)
    ┌─────────────────────────────────────┐
    │                                     │
    │  nginx ─── backend ─── mysql         │
    │  :80       :8080      :3306          │
    │                                     │
    └─────────────────────────────────────┘
```

- Nginx: port 80 exposed to host, SPA fallback + API proxy to `backend:8080`
- Backend: port 8080 internal only, connects to MySQL at `mysql:3306`
- MySQL: port 3306 internal only, healthcheck via `mysqladmin ping`

### Nginx Configuration Highlights

- `/` — SPA routing (`try_files $uri /index.html`)
- `/api/` — reverse proxy to `backend:8080`
- `/uploads/` — proxy to backend static file serving
- Static assets: 1-year cache with `immutable`, gzip compression enabled

---

## Architecture Diagram

```
                    Docker Compose
    ┌───────────────────────────────────────────────────┐
    │                                                   │
    │  ┌──────────┐   ┌──────────┐   ┌───────────────┐  │
    │  │  Nginx   │──▶│SpringBoot│──▶│   MySQL 8.0    │  │
    │  │  :80     │   │  :8080   │   │   :3306        │  │
    │  └──────────┘   └──────────┘   └───────────────┘  │
    │                                                   │
    └───────────────────────────────────────────────────┘
```

---

## Database Schema

### `user` — 用户表

| Column     | Type            | Description                |
|------------|-----------------|----------------------------|
| id         | BIGINT (PK)     | 用户 ID，自增              |
| email      | VARCHAR(128)    | 邮箱（唯一）               |
| password   | VARCHAR(255)    | BCrypt 加密密码            |
| nickname   | VARCHAR(64)     | 昵称                       |
| avatar     | VARCHAR(512)    | 头像 URL                   |
| created_at | DATETIME        | 注册时间                   |

### `couple` — 情侣关系表

| Column      | Type            | Description                |
|-------------|-----------------|----------------------------|
| id          | BIGINT (PK)     | 关系 ID，自增              |
| user1_id    | BIGINT (FK)     | 用户 1 ID                  |
| user2_id    | BIGINT (FK)     | 用户 2 ID                  |
| invite_code | VARCHAR(8)      | 邀请码（唯一，6-8位）      |
| anniversary | DATE            | 纪念日                     |
| created_at  | DATETIME        | 创建时间                   |

### `tree` — 爱情树表

| Column       | Type            | Description                |
|--------------|-----------------|----------------------------|
| id           | BIGINT (PK)     | 树 ID，自增                |
| couple_id    | BIGINT (FK UNIQUE)| 情侣关系 ID（1:1）       |
| stage        | VARCHAR(20)     | 成长阶段（SEED/SPROUT/SAPLING/BLOOM/FRUIT） |
| leaf_count   | INT             | 叶子数                     |
| flower_count | INT             | 花朵数                     |

### `love_event` — 恋爱事件表

| Column     | Type            | Description                |
|------------|-----------------|----------------------------|
| id         | BIGINT (PK)     | 事件 ID，自增              |
| couple_id  | BIGINT (FK)     | 情侣关系 ID                |
| author_id  | BIGINT (FK)     | 记录者 ID                  |
| title      | VARCHAR(128)    | 事件标题                   |
| content    | TEXT            | 事件内容                   |
| event_type | VARCHAR(32)     | 事件类型（first_meet/first_date/travel/anniversary/birthday/daily/other） |
| event_date | DATE            | 事件日期                   |
| images     | JSON            | 关联图片 URL 列表          |
| mood       | VARCHAR(16)     | 心情表情                   |
| created_at | DATETIME        | 创建时间                   |

### `message` — 悄悄话表

| Column     | Type            | Description                |
|------------|-----------------|----------------------------|
| id         | BIGINT (PK)     | 消息 ID，自增              |
| couple_id  | BIGINT (FK)     | 情侣关系 ID                |
| from_id    | BIGINT (FK)     | 发送者 ID                  |
| to_id      | BIGINT (FK)     | 接收者 ID                  |
| content    | TEXT            | 消息内容                   |
| is_read    | TINYINT(1)      | 是否已读 (0/1)             |
| created_at | DATETIME        | 发送时间                   |

### `photo` — 照片表

| Column      | Type            | Description                |
|-------------|-----------------|----------------------------|
| id          | BIGINT (PK)     | 照片 ID，自增              |
| couple_id   | BIGINT (FK)     | 情侣关系 ID                |
| uploader_id | BIGINT (FK)     | 上传者 ID                  |
| url         | VARCHAR(512)    | 照片 URL                   |
| caption     | VARCHAR(256)    | 照片描述                   |
| created_at  | DATETIME        | 上传时间                   |

---

## License

MIT
