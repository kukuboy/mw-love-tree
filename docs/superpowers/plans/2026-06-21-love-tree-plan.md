# LoveTree (爱情树) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a private couple-space web app with a Canvas-drawn love tree that grows with shared memories, plus timeline, messages, photos, and countdown features.

**Architecture:** Docker Compose orchestrates 3 services — Nginx (serves Vue 3 SPA + proxies API), Spring Boot (REST API + MyBatis), MySQL 8.0 (persistent data). Nginx entry on :80, internal communication over `love-net` bridge network.

**Tech Stack:** Vue 3 + TypeScript + Pinia + Vue Router 4 (frontend), Spring Boot 3.x + MyBatis-Plus + JWT (jjwt) + BCrypt (backend), MySQL 8.0 (database), Docker Compose (deployment).

## Global Constraints

- Vue 3.5+ with Composition API + `<script setup>` + TypeScript
- Spring Boot 3.x with JDK 17
- MyBatis-Plus 3.x for ORM (all SQL via XML mappers, `#{}` only — never `${}`)
- MySQL 8.0 with InnoDB, UTF8MB4 charset
- JWT auth, 7-day expiry, BCrypt passwords, secret from env var only
- Unified API response: `{ code, message, data }`
- Frontend visual: warm pink-cream gradient, frosted glass cards, Canvas tree, petal particles
- File uploads: jpg/png/gif/webp only, max 10MB, stored locally in `/app/uploads`
- All couple-scoped data verified against `couple_id` from JWT
- Docker multi-stage builds, images under 250MB

---

### Task 1: Backend Project Scaffold

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/lovetree/LoveTreeApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/schema.sql`

**Interfaces:**
- Produces: Spring Boot app runs on port 8080; MySQL connection from env vars; schema.sql auto-executed on startup; MyBatis-Plus configured

- [ ] **Step 1: Create `backend/pom.xml`** with Spring Boot 3.x parent, dependencies: spring-boot-starter-web, mybatis-plus-boot-starter 3.5.5, mysql-connector-j, jjwt-api 0.12.5, jjwt-impl, jjwt-jackson, spring-boot-starter-validation, lombok, spring-boot-starter-test, h2 (test scope). Java 17.

- [ ] **Step 2: Create `backend/src/main/java/com/lovetree/LoveTreeApplication.java`** — standard Spring Boot main class with `@SpringBootApplication` and `@MapperScan("com.lovetree.mapper")`.

- [ ] **Step 3: Create `backend/src/main/resources/application.yml`** — configure server.port=8080, MySQL datasource with env vars (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`), MyBatis-Plus with `map-underscore-to-camel-case: true` and mapper XML location `classpath:mapper/*.xml`, JWT secret from `JWT_SECRET` env var with default, file upload max-size 10MB, upload dir `/app/uploads`.

- [ ] **Step 4: Create `backend/src/main/resources/schema.sql`** with 6 tables: `user` (id BIGINT AUTO_INCREMENT, email VARCHAR(128) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, nickname VARCHAR(64), avatar VARCHAR(512), created_at DATETIME DEFAULT CURRENT_TIMESTAMP), `couple` (id BIGINT AUTO_INCREMENT, user1_id BIGINT NOT NULL, user2_id BIGINT, invite_code VARCHAR(8) UNIQUE NOT NULL, anniversary DATE, created_at DATETIME), `tree` (id BIGINT AUTO_INCREMENT, couple_id BIGINT UNIQUE NOT NULL, stage VARCHAR(20) DEFAULT 'SEED', leaf_count INT DEFAULT 0, flower_count INT DEFAULT 0), `love_event` (id BIGINT AUTO_INCREMENT, couple_id BIGINT NOT NULL, author_id BIGINT NOT NULL, title VARCHAR(128) NOT NULL, content TEXT, event_type VARCHAR(32) NOT NULL, event_date DATE NOT NULL, images JSON, mood VARCHAR(16), created_at DATETIME), `message` (id BIGINT AUTO_INCREMENT, couple_id BIGINT NOT NULL, from_id BIGINT NOT NULL, to_id BIGINT NOT NULL, content TEXT NOT NULL, is_read TINYINT(1) DEFAULT 0, created_at DATETIME), `photo` (id BIGINT AUTO_INCREMENT, couple_id BIGINT NOT NULL, uploader_id BIGINT NOT NULL, url VARCHAR(512) NOT NULL, caption VARCHAR(256), created_at DATETIME). Add FKs and indexes. Use `IF NOT EXISTS` and `CREATE TABLE IF NOT EXISTS`.

- [ ] **Step 5: Verify** — run `cd backend && mvn compile -q` to confirm no compilation errors.

- [ ] **Step 6: Commit**

```bash
git add backend/pom.xml backend/src/
git commit -m "feat: scaffold Spring Boot backend with pom.xml, schema, and config"
```

---

### Task 2: Backend Common Infrastructure

**Files:**
- Create: `backend/src/main/java/com/lovetree/common/ApiResponse.java`
- Create: `backend/src/main/java/com/lovetree/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/lovetree/common/BusinessException.java`
- Create: `backend/src/main/java/com/lovetree/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/lovetree/config/WebConfig.java`
- Create: `backend/src/main/java/com/lovetree/common/JwtUtil.java`
- Create: `backend/src/main/java/com/lovetree/config/JwtInterceptor.java`
- Create: `backend/src/main/java/com/lovetree/config/CurrentUser.java`
- Create: `backend/src/main/java/com/lovetree/config/CurrentUserResolver.java`

**Interfaces:**
- Produces:
  - `ApiResponse<T>` — generic wrapper with `code`, `message`, `data`, plus static factory methods `success(T data)`, `error(int code, String msg)`
  - `BusinessException` — extends RuntimeException, holds `code` and `message`
  - `GlobalExceptionHandler` — `@ControllerAdvice`, handles BusinessException→ApiResponse, MethodArgumentNotValidException→400, generic Exception→500
  - `JwtUtil` — `generateToken(Long userId, Long coupleId)` returns String, `parseToken(String token)` returns Claims, `getUserId(Claims)` returns Long, `getCoupleId(Claims)` returns Long
  - `SecurityConfig` — `@Configuration`, allows `/api/auth/**` public, everything else requires JWT interceptor
  - `WebConfig` — `@Configuration`, adds `JwtInterceptor` to interceptors, adds `CurrentUserResolver` to argument resolvers, CORS allow all origins in dev
  - `JwtInterceptor` — reads Bearer token, validates via JwtUtil, sets userId+coupleId on request attributes, returns 401 on failure
  - `CurrentUser` annotation + `CurrentUserResolver` — injects user info into controller method params

- [ ] **Step 1: Create `ApiResponse.java`**

```java
package com.lovetree.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **Step 2: Create `BusinessException.java`**

```java
package com.lovetree.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}
```

- [ ] **Step 3: Create `GlobalExceptionHandler.java`**

```java
package com.lovetree.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getCode() == 401 ? 401 : 400)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("validation failed");
        return ResponseEntity.badRequest().body(ApiResponse.error(400, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

- [ ] **Step 4: Create `JwtUtil.java`** — uses `jjwt`. Constructor reads `JWT_SECRET` env var (default `lovetree-secret-change-me`). Methods: `generateToken(Long userId, Long coupleId)` — builds JWT with subject=userId.toString(), claim "coupleId", issuedAt, expiration +7days, signs with HMAC-SHA256. `parseToken(String token)` — parses and validates, returns Claims. `getUserId(Claims)` returns Long. `getCoupleId(Claims)` returns Long (handles null claims returning null).

- [ ] **Step 5: Create `CurrentUser.java`** — annotation `@Target(ElementType.PARAMETER)` `@Retention(RetentionPolicy.RUNTIME)`, with fields `Long userId`, `Long coupleId`.

- [ ] **Step 6: Create `CurrentUserResolver.java`** — implements `HandlerMethodArgumentResolver`. In `resolveArgument`, reads `userId` and `coupleId` from `request.getAttribute()`, instantiates `CurrentUser` with those values. `supportsParameter` returns true when param has `CurrentUser` annotation.

- [ ] **Step 7: Create `JwtInterceptor.java`** — implements `HandlerInterceptor`. PreHandle: gets Authorization header, strips "Bearer ", calls `jwtUtil.parseToken()`, sets userId+coupleId on request attributes. On parse failure or missing header, sets response to 401 with JSON error body, returns false.

- [ ] **Step 8: Create `SecurityConfig.java`** (name it `WebConfig.java` for consistency) — `@Configuration implements WebMvcConfigurer`. Add interceptor with `excludePathPatterns("/api/auth/**")`. Add argument resolver for CurrentUser. Add CORS mapping `allowedOrigins("*")`, `allowedMethods("*")`, `allowedHeaders("*")` for dev convenience.

- [ ] **Step 9: Verify** — `cd backend && mvn compile -q` passes.

- [ ] **Step 10: Commit**

```bash
git add backend/src/main/java/com/lovetree/common/ backend/src/main/java/com/lovetree/config/
git commit -m "feat: add common infrastructure - ApiResponse, JWT, security, exception handling"
```

---

### Task 3: Backend Entity Classes

**Files:**
- Create: `backend/src/main/java/com/lovetree/entity/User.java`
- Create: `backend/src/main/java/com/lovetree/entity/Couple.java`
- Create: `backend/src/main/java/com/lovetree/entity/Tree.java`
- Create: `backend/src/main/java/com/lovetree/entity/LoveEvent.java`
- Create: `backend/src/main/java/com/lovetree/entity/Message.java`
- Create: `backend/src/main/java/com/lovetree/entity/Photo.java`

**Interfaces:**
- Consumes: none (standalone entities)
- Produces: entity POJOs matching schema.sql columns, with Lombok `@Data`, `@TableName`, `@TableId(type = IdType.AUTO)` on id fields

- [ ] **Step 1: Create all 6 entity classes** — each with Lombok `@Data` `@NoArgsConstructor` `@AllArgsConstructor`, MyBatis-Plus `@TableName("table_name")`, `@TableId(type = IdType.AUTO)` on id. Field names use camelCase (MyBatis-Plus auto-maps to snake_case). `LoveEvent.images` is `String` (stores JSON array string). Dates use `LocalDate` / `LocalDateTime`.

```java
// User.java
package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;
}
```

```java
// Couple.java
package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("couple")
public class Couple {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String inviteCode;
    private LocalDate anniversary;
    private LocalDateTime createdAt;
}
```

```java
// Tree.java
package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("tree")
public class Tree {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private String stage;
    private Integer leafCount;
    private Integer flowerCount;
}
```

```java
// LoveEvent.java
package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("love_event")
public class LoveEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long authorId;
    private String title;
    private String content;
    private String eventType;
    private LocalDate eventDate;
    private String images;
    private String mood;
    private LocalDateTime createdAt;
}
```

```java
// Message.java
package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long fromId;
    private Long toId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
```

```java
// Photo.java
package com.lovetree.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@TableName("photo")
public class Photo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coupleId;
    private Long uploaderId;
    private String url;
    private String caption;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Verify** — `cd backend && mvn compile -q` passes.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/lovetree/entity/
git commit -m "feat: add JPA entity classes for all 6 tables"
```

---

### Task 4: Backend Auth Module (Register + Login)

**Files:**
- Create: `backend/src/main/java/com/lovetree/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/lovetree/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/lovetree/dto/AuthResponse.java`
- Create: `backend/src/main/java/com/lovetree/mapper/UserMapper.java`
- Create: `backend/src/main/resources/mapper/UserMapper.xml`
- Create: `backend/src/main/java/com/lovetree/service/AuthService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/AuthServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/controller/AuthController.java`

**Interfaces:**
- Consumes: `User` entity, `UserMapper` interface, `JwtUtil`, `ApiResponse`, `BusinessException`
- Produces: `AuthService` with `register(email, password, nickname): AuthResponse` and `login(email, password): AuthResponse`; `AuthController` with `POST /api/auth/register` and `POST /api/auth/login`

- [ ] **Step 1: Create DTOs**

```java
// RegisterRequest.java
package com.lovetree.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email @NotBlank
    private String email;
    @NotBlank @Size(min = 6, max = 64)
    private String password;
    @NotBlank @Size(max = 32)
    private String nickname;
}

// LoginRequest.java
package com.lovetree.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}

// AuthResponse.java
package com.lovetree.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String nickname;
    private Long coupleId;
}
```

- [ ] **Step 2: Create `UserMapper.java`** interface extending `BaseMapper<User>`, with method `User findByEmail(@Param("email") String email);`.

- [ ] **Step 3: Create `UserMapper.xml`** with `<select id="findByEmail" resultType="com.lovetree.entity.User">SELECT * FROM user WHERE email = #{email}</select>`.

- [ ] **Step 4: Create `AuthService.java`** interface with `AuthResponse register(String email, String password, String nickname)` and `AuthResponse login(String email, String password)`.

- [ ] **Step 5: Create `AuthServiceImpl.java`**:
  - `register`: check email not taken (throw BusinessException 409 "Email already registered"), BCrypt hash password, insert user, generate JWT with userId and coupleId=null, return AuthResponse.
  - `login`: find user by email (throw BusinessException 401 "Invalid credentials" if not found), BCrypt check password (same 401 if mismatch), find couple where user1_id or user2_id = userId, generate JWT including coupleId (or null), return AuthResponse.

- [ ] **Step 6: Create `AuthController.java`**:
  - `POST /api/auth/register` — `@Valid @RequestBody RegisterRequest` → `authService.register()` → `ApiResponse<AuthResponse>`
  - `POST /api/auth/login` — `@Valid @RequestBody LoginRequest` → `authService.login()` → `ApiResponse<AuthResponse>`

- [ ] **Step 7: Verify** — `cd backend && mvn compile -q` passes.

- [ ] **Step 8: Commit**

```bash
git add backend/src/main/java/com/lovetree/dto/ backend/src/main/java/com/lovetree/mapper/ backend/src/main/resources/mapper/ backend/src/main/java/com/lovetree/service/ backend/src/main/java/com/lovetree/controller/
git commit -m "feat: implement auth module - register and login with JWT"
```

---

### Task 5: Backend Couple Module (Pairing)

**Files:**
- Create: `backend/src/main/java/com/lovetree/dto/CoupleInfoResponse.java`
- Create: `backend/src/main/java/com/lovetree/dto/InviteResponse.java`
- Create: `backend/src/main/java/com/lovetree/dto/JoinRequest.java`
- Create: `backend/src/main/java/com/lovetree/mapper/CoupleMapper.java`
- Create: `backend/src/main/java/com/lovetree/mapper/TreeMapper.java`
- Create: `backend/src/main/resources/mapper/CoupleMapper.xml`
- Create: `backend/src/main/resources/mapper/TreeMapper.xml`
- Create: `backend/src/main/java/com/lovetree/service/CoupleService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/CoupleServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/controller/CoupleController.java`

**Interfaces:**
- Consumes: `Couple`, `Tree`, `User` entities; `CoupleMapper`, `TreeMapper`, `UserMapper`; `JwtUtil`, `CurrentUser`, `ApiResponse`, `BusinessException`
- Produces: `CoupleService` with `generateInvite(Long userId): String`, `join(Long userId, String inviteCode): void`, `getInfo(Long coupleId, Long userId): CoupleInfoResponse`

- [ ] **Step 1: Create DTOs** — `CoupleInfoResponse` (partnerNickname, partnerAvatar, inviteCode, anniversary, daysTogether long), `InviteResponse` (inviteCode), `JoinRequest` (inviteCode String @NotBlank).

- [ ] **Step 2: Create `CoupleMapper.java`** extending `BaseMapper<Couple>`, with `Couple findByInviteCode(@Param("code") String code)`, `Couple findByUserId(@Param("userId") Long userId)`. Create XML.

- [ ] **Step 3: Create `TreeMapper.java`** extending `BaseMapper<Tree>`, with `Tree findByCoupleId(@Param("coupleId") Long coupleId)`. Create XML.

- [ ] **Step 4: Create `CoupleServiceImpl.java`**:
  - `generateInvite(userId)`: check user not already in a couple (throw 409), generate random 8-char alphanumeric code, insert couple with user1_id=userId and invite_code=code, insert tree record with stage='SEED', return code.
  - `join(userId, code)`: find couple by inviteCode (throw 404 if not found), verify user2_id is null (throw 409 "already paired"), verify joiner is not user1 (throw 400 "can't pair with yourself"), set user2_id=userId, set anniversary=today, return void.
  - `getInfo(coupleId, userId)`: find couple, find partner user (the one not equal to userId), compute daysTogether from anniversary using ChronoUnit.DAYS, return CoupleInfoResponse.

- [ ] **Step 5: Create `CoupleController.java`**:
  - `POST /api/couple/invite` — `@CurrentUser` → `coupleService.generateInvite()`
  - `POST /api/couple/join` — `@CurrentUser` + `@Valid @RequestBody JoinRequest` → `coupleService.join()`
  - `GET /api/couple/info` — `@CurrentUser` → `coupleService.getInfo()`

- [ ] **Step 6: Verify** — `mvn compile -q` passes.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/lovetree/dto/CoupleInfoResponse.java backend/src/main/java/com/lovetree/dto/InviteResponse.java backend/src/main/java/com/lovetree/dto/JoinRequest.java backend/src/main/java/com/lovetree/mapper/CoupleMapper.java backend/src/main/java/com/lovetree/mapper/TreeMapper.java backend/src/main/resources/mapper/ backend/src/main/java/com/lovetree/service/CoupleService.java backend/src/main/java/com/lovetree/service/impl/CoupleServiceImpl.java backend/src/main/java/com/lovetree/controller/CoupleController.java
git commit -m "feat: implement couple pairing module - invite, join, info"
```

---

### Task 6: Backend Tree & Event Module

**Files:**
- Create: `backend/src/main/java/com/lovetree/dto/TreeResponse.java`
- Create: `backend/src/main/java/com/lovetree/dto/TreeStatsResponse.java`
- Create: `backend/src/main/java/com/lovetree/dto/CreateEventRequest.java`
- Create: `backend/src/main/java/com/lovetree/dto/EventResponse.java`
- Create: `backend/src/main/java/com/lovetree/mapper/LoveEventMapper.java`
- Create: `backend/src/main/resources/mapper/LoveEventMapper.xml`
- Create: `backend/src/main/java/com/lovetree/service/TreeService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/TreeServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/service/EventService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/EventServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/controller/TreeController.java`
- Create: `backend/src/main/java/com/lovetree/controller/EventController.java`

**Interfaces:**
- Consumes: Tree, LoveEvent entities; TreeMapper, LoveEventMapper; CoupleService (for couple_id lookup); CurrentUser
- Produces: Tree REST API (GET tree state, GET stats), Event CRUD API (GET list paginated, POST create, GET by id, PUT update, DELETE)

- [ ] **Step 1: Create DTOs**

`TreeResponse` (stage, leafCount, flowerCount, stageLabel — the emoji+Chinese label), `TreeStatsResponse` (totalEvents, typeDistribution as Map<String,Integer>, monthlyCounts as Map<String,Integer>). `CreateEventRequest` (title @NotBlank, content, eventType @NotBlank, eventDate @NotNull, images as List<String>, mood). `EventResponse` (all LoveEvent fields + authorNickname String, images parsed to List<String>).

- [ ] **Step 2: Create `TreeServiceImpl.java`**:
  - `getTree(coupleId)`: find tree by coupleId, determine stageLabel by mapping stage enum → Chinese label (SEED→"🌱 种子", SPROUT→"🌿 嫩芽", SAPLING→"🪴 小树", BLOOM→"🌸 花树", FRUIT→"🍊 果树"), return TreeResponse.
  - `getStats(coupleId)`: count events by coupleId; group by eventType using `eventService.getTypeDistribution(coupleId)`; group by month for heatmap. Return TreeStatsResponse.

- [ ] **Step 3: Create `EventServiceImpl.java`**:
  - `list(coupleId, page, size, eventType)`: query events by coupleId, optionally filter by eventType, order by event_date DESC, paginate with MyBatis-Plus Page. Join with user table to get authorNickname. Map images string→List.
  - `create(coupleId, authorId, request)`: validate eventType is one of valid types (throw 400), JSON-serialize images list to String, insert event. Then update tree: increment leafCount by 1 (and flowerCount by 1 if eventType is first_meet/first_date/anniversary/birthday). Recalculate stage based on new count using helper `determineStage(int count)` which returns stage string. Save updated tree. Return created EventResponse.
  - `getById(id, coupleId)`: find event by id, verify coupleId matches (throw 403 if not), return EventResponse with authorNickname.
  - `update(id, coupleId, authorId, request)`: verify ownership and coupleId, update fields, return updated EventResponse.
  - `delete(id, coupleId)`: verify coupleId, delete event, decrement tree leafCount (and flowerCount if applicable), recalculate stage, save tree.

- [ ] **Step 4: Create `TreeController.java`** — `GET /api/tree` and `GET /api/tree/stats`, both use `@CurrentUser` to get coupleId, call treeService.

- [ ] **Step 5: Create `EventController.java`**:
  - `GET /api/events?page=1&size=20&eventType=first_meet` → EventService.list()
  - `POST /api/events` → EventService.create()
  - `GET /api/events/{id}` → EventService.getById()
  - `PUT /api/events/{id}` → EventService.update()
  - `DELETE /api/events/{id}` → EventService.delete()

- [ ] **Step 6: Verify** — `mvn compile -q` passes.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/lovetree/dto/TreeResponse.java backend/src/main/java/com/lovetree/dto/TreeStatsResponse.java backend/src/main/java/com/lovetree/dto/CreateEventRequest.java backend/src/main/java/com/lovetree/dto/EventResponse.java backend/src/main/java/com/lovetree/mapper/LoveEventMapper.java backend/src/main/resources/mapper/LoveEventMapper.xml backend/src/main/java/com/lovetree/service/TreeService.java backend/src/main/java/com/lovetree/service/impl/TreeServiceImpl.java backend/src/main/java/com/lovetree/service/EventService.java backend/src/main/java/com/lovetree/service/impl/EventServiceImpl.java backend/src/main/java/com/lovetree/controller/TreeController.java backend/src/main/java/com/lovetree/controller/EventController.java
git commit -m "feat: implement tree and event modules with growth logic"
```

---

### Task 7: Backend Message & Photo Modules

**Files:**
- Create: `backend/src/main/java/com/lovetree/dto/SendMessageRequest.java`
- Create: `backend/src/main/java/com/lovetree/dto/MessageResponse.java`
- Create: `backend/src/main/java/com/lovetree/dto/PhotoResponse.java`
- Create: `backend/src/main/java/com/lovetree/mapper/MessageMapper.java`
- Create: `backend/src/main/resources/mapper/MessageMapper.xml`
- Create: `backend/src/main/java/com/lovetree/mapper/PhotoMapper.java`
- Create: `backend/src/main/resources/mapper/PhotoMapper.xml`
- Create: `backend/src/main/java/com/lovetree/service/MessageService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/MessageServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/service/PhotoService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/PhotoServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/controller/MessageController.java`
- Create: `backend/src/main/java/com/lovetree/controller/PhotoController.java`

**Interfaces:**
- Consumes: Message, Photo entities; their mappers; CoupleService for partner lookup; CurrentUser
- Produces: Message REST API (list, send, unread count, mark read), Photo REST API (list, upload multipart, delete)

- [ ] **Step 1: Create DTOs**

`SendMessageRequest` (content @NotBlank), `MessageResponse` (id, fromId, toId, content, isRead, createdAt, fromNickname), `PhotoResponse` (id, url, caption, uploaderId, uploaderNickname, createdAt).

- [ ] **Step 2: Create `MessageServiceImpl.java`**:
  - `getMessages(coupleId, userId)`: select messages where coupleId = coupleId AND (from_id = userId OR to_id = userId), order by created_at DESC, join user for nicknames. Return List<MessageResponse>.
  - `send(coupleId, fromId, request)`: get partner's userId from couple (user1_id or user2_id that != fromId), insert message with to_id = partner id. Return created MessageResponse.
  - `getUnreadCount(coupleId, userId)`: count where coupleId = ? AND to_id = ? AND is_read = 0.
  - `markRead(messageId, coupleId)`: update is_read = 1 where id = ? AND couple_id = ? (verify couple scope).

- [ ] **Step 3: Create `PhotoServiceImpl.java`**:
  - `upload(coupleId, uploaderId, file MultipartFile, caption)`: validate file extension (jpg/png/gif/webp only), generate unique filename with UUID, save to `/app/uploads/` directory, insert photo record with url = `/uploads/{filename}`. Return PhotoResponse.
  - `list(coupleId, page, size)`: paginated query, join user for uploaderNickname, order by created_at DESC.
  - `delete(photoId, coupleId)`: verify couple scope, delete file from disk, delete record.

- [ ] **Step 4: Create `MessageController.java`**:
  - `GET /api/messages` → messageService.getMessages()
  - `POST /api/messages` → messageService.send()
  - `GET /api/messages/unread-count` → messageService.getUnreadCount()
  - `PUT /api/messages/{id}/read` → messageService.markRead()

- [ ] **Step 5: Create `PhotoController.java`**:
  - `GET /api/photos?page=1&size=20` → photoService.list()
  - `POST /api/photos` → `@RequestParam("file") MultipartFile file, @RequestParam(required=false) String caption` → photoService.upload()
  - `DELETE /api/photos/{id}` → photoService.delete()

- [ ] **Step 6: Verify** — `mvn compile -q` passes.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/lovetree/dto/SendMessageRequest.java backend/src/main/java/com/lovetree/dto/MessageResponse.java backend/src/main/java/com/lovetree/dto/PhotoResponse.java backend/src/main/java/com/lovetree/mapper/MessageMapper.java backend/src/main/resources/mapper/MessageMapper.xml backend/src/main/java/com/lovetree/mapper/PhotoMapper.java backend/src/main/resources/mapper/PhotoMapper.xml backend/src/main/java/com/lovetree/service/MessageService.java backend/src/main/java/com/lovetree/service/impl/MessageServiceImpl.java backend/src/main/java/com/lovetree/service/PhotoService.java backend/src/main/java/com/lovetree/service/impl/PhotoServiceImpl.java backend/src/main/java/com/lovetree/controller/MessageController.java backend/src/main/java/com/lovetree/controller/PhotoController.java
git commit -m "feat: implement message and photo upload modules"
```

---

### Task 8: Backend Special Days & Static Resource Config

**Files:**
- Create: `backend/src/main/java/com/lovetree/dto/SpecialDayResponse.java`
- Create: `backend/src/main/java/com/lovetree/service/SpecialDayService.java`
- Create: `backend/src/main/java/com/lovetree/service/impl/SpecialDayServiceImpl.java`
- Create: `backend/src/main/java/com/lovetree/controller/SpecialDayController.java`
- Modify: `backend/src/main/java/com/lovetree/config/WebConfig.java`

**Interfaces:**
- Consumes: CoupleService, LoveEventMapper, CurrentUser
- Produces: `GET /api/special-days` returning anniversaries list and next countdown

- [ ] **Step 1: Create `SpecialDayResponse.java`** — fields: name (String), date (LocalDate), daysUntil (int), daysTogether (long, total days since anniversary if applicable).

- [ ] **Step 2: Create `SpecialDayServiceImpl.java`** — from couple, get anniversary; calculate daysTogether from anniversary to today; find upcoming anniversary (next anniversary date); count daysUntil. Also pull events with type=anniversary as additional special days. Return list sorted by daysUntil ascending.

- [ ] **Step 3: Create `SpecialDayController.java`** — `GET /api/special-days`, uses @CurrentUser.

- [ ] **Step 4: Update `WebConfig.java`** — add static resource mapping for `/uploads/**` → `file:/app/uploads/` so uploaded photos are served.

- [ ] **Step 5: Verify** — `mvn compile -q` passes.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/lovetree/dto/SpecialDayResponse.java backend/src/main/java/com/lovetree/service/SpecialDayService.java backend/src/main/java/com/lovetree/service/impl/SpecialDayServiceImpl.java backend/src/main/java/com/lovetree/controller/SpecialDayController.java backend/src/main/java/com/lovetree/config/WebConfig.java
git commit -m "feat: implement special days countdown and static resource serving"
```

---

### Task 9: Backend Dockerfile & Build Verification

**Files:**
- Create: `backend/Dockerfile`
- Create: `backend/.dockerignore`

**Interfaces:**
- Consumes: full backend project
- Produces: Docker image running Spring Boot on :8080

- [ ] **Step 1: Create `backend/.dockerignore`** — ignore `target/`, `.git/`, `.idea/`, `*.iml`, `node_modules/`.

- [ ] **Step 2: Create `backend/Dockerfile`** with multi-stage build:
  - Stage 1 (build): `FROM maven:3.9-eclipse-temurin-17 AS build`, `WORKDIR /app`, `COPY pom.xml .`, `RUN mvn dependency:go-offline -q`, `COPY src ./src`, `RUN mvn package -DskipTests -q`.
  - Stage 2 (run): `FROM eclipse-temurin:17-jre-alpine`, `WORKDIR /app`, install curl for healthcheck, `COPY --from=build /app/target/*.jar app.jar`, `EXPOSE 8080`, healthcheck with `curl -f http://localhost:8080/actuator/health || exit 1`, `ENTRYPOINT ["java", "-jar", "app.jar"]`.

- [ ] **Step 3: Verify build** — run `docker build -t lovetree-backend ./backend` from project root, confirm image builds and is under 250MB (`docker images lovetree-backend`).

- [ ] **Step 4: Commit**

```bash
git add backend/Dockerfile backend/.dockerignore
git commit -m "feat: add backend Dockerfile with multi-stage build"
```

---

### Task 10: Frontend Project Scaffold

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.node.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/env.d.ts`

**Interfaces:**
- Produces: `npm run dev` starts Vue 3 dev server with HMR on port 5173, proxying `/api` to localhost:8080

- [ ] **Step 1: Create `frontend/package.json`** with dependencies: vue 3.5+, vue-router 4, pinia, axios. DevDependencies: typescript, vite, @vitejs/plugin-vue, vue-tsc, vitest, @vue/test-utils.

- [ ] **Step 2: Create `frontend/vite.config.ts`** — Vue plugin, resolve alias `@` → `./src`, dev server proxy `"/api"` → `"http://localhost:8080"` and `"/uploads"` → `"http://localhost:8080"`.

- [ ] **Step 3: Create `frontend/index.html`** — basic HTML5 shell with `<div id="app">` and `<script type="module" src="/src/main.ts">`.

- [ ] **Step 4: Create `frontend/src/main.ts`** — `createApp(App).use(router).use(createPinia()).mount('#app')`.

- [ ] **Step 5: Create `frontend/src/App.vue`** — minimal wrapper with `<RouterView />`.

- [ ] **Step 6: Create `frontend/src/env.d.ts`** — declare module `*.vue`, Vite env types.

- [ ] **Step 7: Run `cd frontend && npm install && npm run dev`** — confirm dev server starts.

- [ ] **Step 8: Commit**

```bash
git add frontend/package.json frontend/tsconfig.json frontend/tsconfig.node.json frontend/vite.config.ts frontend/index.html frontend/src/main.ts frontend/src/App.vue frontend/src/env.d.ts
git commit -m "feat: scaffold Vue 3 + TypeScript frontend with Vite"
```

---

### Task 11: Frontend Global Styles & Theme

**Files:**
- Create: `frontend/src/styles/variables.css`
- Create: `frontend/src/styles/global.css`
- Create: `frontend/src/styles/animations.css`

- [ ] **Step 1: Create `variables.css`** — CSS custom properties defining the color palette: `--color-primary: #E8A0BF`, `--color-secondary: #F3CCD5`, `--color-accent: #C5898E`, `--color-bg: #FFF5F7`, `--color-text: #4A3B3C`, `--color-gold: #D4A574`. Also border-radius vars, shadow vars, font-family: `"PingFang SC", "Noto Serif SC", sans-serif`.

- [ ] **Step 2: Create `global.css`** — imports variables.css; sets `*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0 }`, body background with warm gradient `linear-gradient(135deg, #FFF5F7 0%, #FDE8EC 50%, #FFF0F3 100%)`, body min-height 100vh, text color, font-family. Style `::-webkit-scrollbar` with thin warm tracks. Define `.card` class: frosted glass `background: rgba(255,255,255,0.6)`, `backdrop-filter: blur(16px)`, `border-radius: 16px`, `box-shadow: 0 4px 24px rgba(232, 160, 191, 0.15)`, `border: 1px solid rgba(255,255,255,0.8)`.

- [ ] **Step 3: Create `animations.css`** — keyframes: `@keyframes float { 0%,100% { transform: translateY(0) rotate(0deg) } 50% { transform: translateY(-20px) rotate(6deg) } }`, `@keyframes petal-fall { from { transform: translateY(-10vh) rotate(0deg); opacity: 1 } to { transform: translateY(110vh) rotate(720deg); opacity: 0 } }`, `@keyframes fadeInUp { from { opacity:0; transform: translateY(24px) } to { opacity:1; transform: translateY(0) } }`, `@keyframes pulse-glow { 0%,100% { box-shadow: 0 0 4px var(--color-primary) } 50% { box-shadow: 0 0 20px var(--color-primary) } }`. Utility classes: `.animate-fade-in-up`, `.animate-float`.

- [ ] **Step 4: Update `main.ts`** — add `import './styles/global.css'`.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/styles/
git commit -m "feat: add global styles, CSS variables, and animations for dreamy aesthetic"
```

---

### Task 12: Frontend Router Setup

**Files:**
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/pages/LoginPage.vue` (stub)
- Create: `frontend/src/pages/RegisterPage.vue` (stub)
- Create: `frontend/src/pages/InvitePage.vue` (stub)
- Create: `frontend/src/pages/DashboardPage.vue` (stub)
- Create: `frontend/src/pages/TimelinePage.vue` (stub)
- Create: `frontend/src/pages/EventFormPage.vue` (stub)
- Create: `frontend/src/pages/EventDetailPage.vue` (stub)
- Create: `frontend/src/pages/MessagesPage.vue` (stub)
- Create: `frontend/src/pages/MessageDetailPage.vue` (stub)
- Create: `frontend/src/pages/AlbumPage.vue` (stub)
- Create: `frontend/src/pages/SettingsPage.vue` (stub)

- [ ] **Step 1: Create `frontend/src/router/index.ts`** — define all 11 routes from spec. Auth guard: `beforeEach` checks `authStore.isLoggedIn` for routes requiring auth; if not logged in, redirect to `/login`. If logged in and visiting `/login` or `/register`, redirect to `/dashboard`. Lazy-load all page components.

- [ ] **Step 2: Create stub page files** — each page exports a minimal Vue SFC with `<template><div>PageName</div></template>` and `<script setup lang="ts"></script>`. This ensures router works before full page implementation.

- [ ] **Step 3: Verify** — run `npm run dev`, visit `/login` and `/register` (should show stubs without redirect). Confirm auth guard works by trying `/dashboard` (should redirect to login).

- [ ] **Step 4: Commit**

```bash
git add frontend/src/router/ frontend/src/pages/
git commit -m "feat: add Vue Router with auth guards and stub pages"
```

---

### Task 13: Frontend API Layer

**Files:**
- Create: `frontend/src/api/client.ts`
- Create: `frontend/src/api/auth.ts`
- Create: `frontend/src/api/couple.ts`
- Create: `frontend/src/api/tree.ts`
- Create: `frontend/src/api/events.ts`
- Create: `frontend/src/api/messages.ts`
- Create: `frontend/src/api/photos.ts`
- Create: `frontend/src/api/specialDays.ts`
- Create: `frontend/src/api/index.ts`

**Interfaces:**
- Produces: typed API functions for all REST endpoints; Axios instance with baseURL `/api`, JWT interceptor, error response interceptor

- [ ] **Step 1: Create `client.ts`** — create Axios instance with `baseURL: '/api'`, request interceptor that reads token from `localStorage.getItem('token')` and sets `Authorization: Bearer xxx`, response interceptor that catches 401 → clear localStorage, redirect to `/login`; generic errors → reject with message.

- [ ] **Step 2: Create `auth.ts`** — `login(data: LoginRequest): Promise<AuthResponse>`, `register(data: RegisterRequest): Promise<AuthResponse>`.

- [ ] **Step 3: Create `couple.ts`** — `getInviteCode(): Promise<InviteResponse>`, `joinCouple(code: string): Promise<void>`, `getCoupleInfo(): Promise<CoupleInfoResponse>`.

- [ ] **Step 4: Create `tree.ts`** — `getTree(): Promise<TreeResponse>`, `getTreeStats(): Promise<TreeStatsResponse>`.

- [ ] **Step 5: Create `events.ts`** — `getEvents(params: {page, size, eventType?}): Promise<PageResult<EventResponse>>`, `createEvent(data: CreateEventRequest): Promise<EventResponse>`, `getEvent(id: number): Promise<EventResponse>`, `updateEvent(id, data): Promise<EventResponse>`, `deleteEvent(id): Promise<void>`.

- [ ] **Step 6: Create `messages.ts`** — `getMessages(): Promise<MessageResponse[]>`, `sendMessage(content: string): Promise<MessageResponse>`, `getUnreadCount(): Promise<number>`, `markRead(id: number): Promise<void>`.

- [ ] **Step 7: Create `photos.ts`** — `getPhotos(page, size): Promise<PageResult<PhotoResponse>>`, `uploadPhoto(file: File, caption?: string): Promise<PhotoResponse>` (uses FormData), `deletePhoto(id): Promise<void>`.

- [ ] **Step 8: Create `specialDays.ts`** — `getSpecialDays(): Promise<SpecialDayResponse[]>`.

- [ ] **Step 9: Create `index.ts`** — barrel export of all API modules.

- [ ] **Step 10: Commit**

```bash
git add frontend/src/api/
git commit -m "feat: add typed API layer with Axios, JWT interceptors, all endpoints"
```

---

### Task 14: Frontend Pinia Stores

**Files:**
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/stores/tree.ts`
- Create: `frontend/src/stores/couple.ts`
- Create: `frontend/src/types/index.ts`

**Interfaces:**
- Produces: `useAuthStore` (isLoggedIn, userId, nickname, coupleId, login(), register(), logout(), checkAuth()), `useTreeStore` (tree, stats, fetchTree(), fetchStats()), `useCoupleStore` (partnerInfo, inviteCode, fetchCoupleInfo(), createInvite(), join())

- [ ] **Step 1: Create `types/index.ts`** — TypeScript interfaces for all DTOs: `LoginRequest`, `RegisterRequest`, `AuthResponse`, `InviteResponse`, `CoupleInfoResponse`, `TreeResponse`, `TreeStatsResponse`, `CreateEventRequest`, `EventResponse`, `MessageResponse`, `PhotoResponse`, `SpecialDayResponse`, `PageResult<T>`. Match backend DTOs exactly.

- [ ] **Step 2: Create `stores/auth.ts`** — Pinia defineStore. State: token (string|null), userId, nickname, coupleId, isLoggedIn. Actions: `login(email, password)` calls authApi.login, stores token in localStorage and state, sets user info. `register(email, password, nickname)` calls authApi.register, same storage. `logout()` clears state and localStorage, router.push('/login'). `checkAuth()` reads token from localStorage, if exists sets isLoggedIn=true. Getters: none.

- [ ] **Step 3: Create `stores/tree.ts`** — state: tree (TreeResponse|null), stats (TreeStatsResponse|null), loading. Actions: `fetchTree()`, `fetchStats()`.

- [ ] **Step 4: Create `stores/couple.ts`** — state: partnerInfo (CoupleInfoResponse|null), inviteCode (string|null), loading. Actions: `fetchCoupleInfo()`, `createInvite()`, `joinCouple(code)`.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/stores/ frontend/src/types/
git commit -m "feat: add Pinia stores for auth, tree, and couple state"
```

---

### Task 15: Frontend Login, Register & Invite Pages

**Files:**
- Create: `frontend/src/pages/LoginPage.vue`
- Create: `frontend/src/pages/RegisterPage.vue`
- Create: `frontend/src/pages/InvitePage.vue`

- [ ] **Step 1: Create `LoginPage.vue`** — centered card layout (max-width 420px, frosted glass card). Branding area: "🌸 LoveTree" title with subtitle "属于我们的爱情树". Email + password input fields with warm pink focus borders. "登录" button (full width, pink gradient bg, hover scale). Link to register page. On submit, calls `authStore.login()`, on success router.push('/dashboard'). Loading state on button. Error toast via simple inline error message. Visual: floating petal particles in CSS background (use animation.css keyframes).

- [ ] **Step 2: Create `RegisterPage.vue`** — similar layout. Fields: nickname, email, password, confirm password (client-side match validation). "注册" button. Links to login page. Calls `authStore.register()`, on success checks if coupleId exists — if not, redirect to `/invite`, else redirect to `/dashboard`.

- [ ] **Step 3: Create `InvitePage.vue`** — two tabs/cards side by side: "创建邀请码" and "加入对方". Left card: if no invite code yet, show "生成邀请码" button, on click calls `coupleStore.createInvite()`, shows generated code prominently (large font, copy button). Right card: input for invite code + "加入" button, calls `coupleStore.joinCouple()`, on success redirect to `/dashboard`. Both cards frosted glass style.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/pages/LoginPage.vue frontend/src/pages/RegisterPage.vue frontend/src/pages/InvitePage.vue
git commit -m "feat: implement login, register, and invite pairing pages"
```

---

### Task 16: Frontend Authenticated Layout (Header + SideNav)

**Files:**
- Create: `frontend/src/components/AppHeader.vue`
- Create: `frontend/src/components/SideNav.vue`
- Create: `frontend/src/layouts/AuthenticatedLayout.vue`

- [ ] **Step 1: Create `AppHeader.vue`** — fixed top header (height 64px), frosted glass bg. Left: "🌸 LoveTree" logo text. Center: days together counter from `coupleStore.partnerInfo?.daysTogether` displayed as "在一起 第 X 天" with heart emoji. Right: user avatar (small circle, default gradient circle with first letter of nickname), dropdown on click with "设置" and "退出登录". Mobile responsive.

- [ ] **Step 2: Create `SideNav.vue`** — desktop: vertical sidebar (width 200px, frosted glass), navigation items with icons: 🌳 爱情树 (dashboard), 📜 时间线 (timeline), 💌 悄悄话 (messages), 🖼️ 相册 (album), ⚙️ 设置 (settings). Active route highlighted with pink bg. Mobile: fixed bottom tab bar with same items (icon+label, smaller). Uses `useRoute()` for active detection. Each item is `router-link`.

- [ ] **Step 3: Create `AuthenticatedLayout.vue`** — `<AppHeader />` at top, `<SideNav />` on left (desktop) or bottom (mobile), `<router-view />` in main content area with padding (left: 200px on desktop, 0 on mobile; top: 64px; bottom: 64px on mobile). Use CSS media query `@media (max-width: 768px)` for mobile layout.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/AppHeader.vue frontend/src/components/SideNav.vue frontend/src/layouts/AuthenticatedLayout.vue
git commit -m "feat: add authenticated layout with header, sidebar nav, and mobile bottom tabs"
```

---

### Task 17: Frontend Dashboard Page & LoveTreeCanvas

**Files:**
- Create: `frontend/src/pages/DashboardPage.vue`
- Create: `frontend/src/components/LoveTreeCanvas.vue`
- Create: `frontend/src/components/TreeStageBadge.vue`
- Create: `frontend/src/components/CountdownCard.vue`
- Create: `frontend/src/components/RecentEvents.vue`

- [ ] **Step 1: Create `LoveTreeCanvas.vue`** — the soul component. Takes `stage` (String), `leafCount` (Number), `flowerCount` (Number) as props. Uses `<canvas>` element with `onMounted` drawing logic:
  - Clear canvas, set dimensions to container size (responsive, listen to resize).
  - **Trunk:** draw curved trunk using quadratic/cubic bezier curves, warm brown `#8B6F5C`, rough brush-like strokes (multiple overlapping thin lines with slight random offsets). Trunk gets taller/thicker with stage.
  - **Branches:** recursive branch drawing function. More branches as stage advances (SEED=0, SPROUT=2, SAPLING=5, BLOOM=8, FRUIT=12). Each branch splits into 2-3 smaller branches. Slight random angles for hand-drawn feel.
  - **Leaves:** draw leaf shapes (ellipse with pointed tip) at branch ends. Color varies by event type stored in... actually, leaves are generic green (`#7DB87D` variations). Count based on `leafCount` min(stage_max, count).
  - **Flowers:** 5-petal flowers (draw 5 circles around center point, with small center circle). Pink/red/purple/orange variations. Count based on `flowerCount`.
  - **SEED stage:** a single seed shape buried in dirt mound, small sparkle effect.
  - **SPROUT stage:** tiny green sprout with two small leaves.
  - **SAPLING stage:** small tree, trunk visible, 4-6 branches, 5-10 leaves.
  - **BLOOM stage:** full tree, 8-12 branches, many leaves + 10-20 flowers, some falling petals animation.
  - **FRUIT stage:** large tree, 12-16 branches, dense leaves, flowers + small golden fruit shapes.
  - **Particle effects:** draw floating petals (pink translucent ellipses) falling slowly, and tiny sparkle stars blinking. Use requestAnimationFrame for continuous animation.

- [ ] **Step 2: Create `TreeStageBadge.vue`** — displays stage emoji+label in a frosted pill badge. Props: stage, stageLabel. Subtle pulse-glow animation.

- [ ] **Step 3: Create `CountdownCard.vue`** — shows next upcoming special day. Props: name, date, daysUntil. Large number for daysUntil with "天后" text. If today (daysUntil=0), show "🎉 就是今天！" with celebratory animation.

- [ ] **Step 4: Create `RecentEvents.vue`** — shows last 3 events as small cards. Each card: eventType emoji, title, eventDate relative text ("3天前"), mood emoji. Click navigates to event detail. "查看全部" link to timeline.

- [ ] **Step 5: Create `DashboardPage.vue`** — onMounted fetches tree (`treeStore.fetchTree()`), couple info (`coupleStore.fetchCoupleInfo()`), special days. Assembles layout: top row = LoveTreeCanvas (large, center), overlay TreeStageBadge; right side (desktop) = CountdownCard + tree stats; below = RecentEvents. Mobile: stacked vertically.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/pages/DashboardPage.vue frontend/src/components/LoveTreeCanvas.vue frontend/src/components/TreeStageBadge.vue frontend/src/components/CountdownCard.vue frontend/src/components/RecentEvents.vue
git commit -m "feat: implement dashboard with animated love tree canvas and widgets"
```

---

### Task 18: Frontend Timeline & Event Pages

**Files:**
- Create: `frontend/src/pages/TimelinePage.vue`
- Create: `frontend/src/components/EventCard.vue`
- Create: `frontend/src/components/EventFilter.vue`
- Create: `frontend/src/pages/EventFormPage.vue`
- Create: `frontend/src/pages/EventDetailPage.vue`
- Create: `frontend/src/components/MoodPicker.vue`
- Create: `frontend/src/components/ImageUploader.vue`
- Create: `frontend/src/components/ImageLightbox.vue`

- [ ] **Step 1: Create `EventCard.vue`** — frosted card showing event. Left side: event type icon (mapped emoji), colored left border by event type. Body: title (bold), content preview (2 lines max, ellipsis), mood emoji. Footer: eventDate formatted as "YYYY年MM月DD日", author nickname, image count if any. Click emits to navigate to detail.

- [ ] **Step 2: Create `EventFilter.vue`** — horizontal scrollable pill chips for each event type: 全部, 💕初次见面, 💗初次约会, ✈️旅行, 🎂生日, 💝纪念日, 📝日常, 📌其他. Active chip highlighted. Emits `filter-change` with eventType.

- [ ] **Step 3: Create `TimelinePage.vue`** — onMounted loads events with pagination. Top: EventFilter. Body: EventCard list (vertical timeline with connecting line on left — CSS pseudo-element vertical line with dots at each card). Infinite scroll: detect scroll to bottom, load next page. Empty state: "还没有一起的记录，去添加第一个吧～" with a button to `/event/new`. Loading: skeleton card placeholders.

- [ ] **Step 4: Create `MoodPicker.vue`** — 4 mood options as large emoji buttons: 😊 开心, 😍 幸福, 🥰 甜蜜, 😘 浪漫. Selected one scales up and glows.

- [ ] **Step 5: Create `ImageUploader.vue`** — drag & drop zone or click to select. File input with accept="image/jpeg,image/png,image/gif,image/webp". Preview thumbnails with remove button. Max 9 images, max 10MB each. Client-side validation.

- [ ] **Step 6: Create `EventFormPage.vue`** — form fields: title (input), eventType (select with emoji+label options), eventDate (date picker), content (textarea), mood (MoodPicker), images (ImageUploader). Submit button: "💖 添加到爱情树". Both create and edit mode (route query param `?edit=id` pre-fills form, on submit calls update API). On success: router.push to detail page, with brief bloom animation overlay.

- [ ] **Step 7: Create `EventDetailPage.vue`** — displays full event info: eventType badge, title, date, author, mood, full content, image gallery (click opens ImageLightbox). Edit and delete buttons (only shown for author). Delete shows confirmation modal "确定要删除这个回忆吗？树会少一片叶子哦 🥺". On delete, router back to timeline.

- [ ] **Step 8: Create `ImageLightbox.vue`** — fullscreen overlay with image, prev/next arrows for gallery, close button (X or click outside). Smooth fade transition.

- [ ] **Step 9: Commit**

```bash
git add frontend/src/pages/TimelinePage.vue frontend/src/components/EventCard.vue frontend/src/components/EventFilter.vue frontend/src/pages/EventFormPage.vue frontend/src/pages/EventDetailPage.vue frontend/src/components/MoodPicker.vue frontend/src/components/ImageUploader.vue frontend/src/components/ImageLightbox.vue
git commit -m "feat: implement timeline, event create/edit/detail with image upload and lightbox"
```

---

### Task 19: Frontend Messages Pages

**Files:**
- Create: `frontend/src/pages/MessagesPage.vue`
- Create: `frontend/src/pages/MessageDetailPage.vue`
- Create: `frontend/src/components/MessageBubble.vue`
- Create: `frontend/src/components/MessageInput.vue`

- [ ] **Step 1: Create `MessageBubble.vue`** — props: content, isMine (boolean), createdAt. Bubble aligned left (partner, pink bg) or right (me, white bg), rounded corners, timestamp below. Tail/arrow on bubble. Smooth appear animation.

- [ ] **Step 2: Create `MessageInput.vue`** — textarea with auto-grow, send button with 💌 icon. Enter to send (shift+enter for newline). Emits `send` with content. Disabled while sending.

- [ ] **Step 3: Create `MessageDetailPage.vue`** — chat-like interface. Header: partner nickname + avatar. Body: scrollable message list (MessageBubble for each), auto-scroll to bottom on new message. Footer: MessageInput. Poll unread count on mount but mark all as read. Empty: "还没有悄悄话，发送第一条吧～".

- [ ] **Step 4: Create `MessagesPage.vue`** — since this is a couple space, there's only one conversation (with partner). This page could simply show the partner card with unread count and link to MessageDetailPage. Display partner info card: avatar, nickname, last message preview, unread badge. Click → navigate to `/messages/${partnerId}`.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/MessagesPage.vue frontend/src/pages/MessageDetailPage.vue frontend/src/components/MessageBubble.vue frontend/src/components/MessageInput.vue
git commit -m "feat: implement messages list and chat interface"
```

---

### Task 20: Frontend Album & Settings Pages

**Files:**
- Create: `frontend/src/pages/AlbumPage.vue`
- Create: `frontend/src/components/PhotoCard.vue`
- Create: `frontend/src/components/PhotoUploader.vue`
- Create: `frontend/src/pages/SettingsPage.vue`
- Create: `frontend/src/components/AvatarUploader.vue`
- Create: `frontend/src/components/CoupleInfo.vue`

- [ ] **Step 1: Create `PhotoCard.vue`** — image thumbnail with frosted overlay showing caption on hover. Aspect ratio 1:1 or 4:3 in a CSS grid. Click opens full image. Delete button (X) appears on hover.

- [ ] **Step 2: Create `PhotoUploader.vue`** — similar to ImageUploader but simpler: single/多 file upload with progress bar. After upload, photo appears in grid.

- [ ] **Step 3: Create `AlbumPage.vue`** — CSS grid gallery (3 columns desktop, 2 tablet, 1 mobile). Top: "添加照片" button triggers upload modal/area. Pagination or infinite scroll. Empty state: "还没有照片，上传你们的第一张合影吧 📸".

- [ ] **Step 4: Create `SettingsPage.vue`** — sections:
  - Profile: nickname edit, avatar upload (AvatarUploader component — circular crop preview, file upload).
  - Couple info: display partner nickname + avatar, invite code with copy button, anniversary date (read-only).
  - Danger zone: "解除配对" button with confirmation (red text, requires typing "确认解除" to proceed).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/AlbumPage.vue frontend/src/components/PhotoCard.vue frontend/src/components/PhotoUploader.vue frontend/src/pages/SettingsPage.vue frontend/src/components/AvatarUploader.vue frontend/src/components/CoupleInfo.vue
git commit -m "feat: implement album gallery and settings/profile pages"
```

---

### Task 21: Frontend Dockerfile & Nginx Config

**Files:**
- Create: `frontend/nginx.conf`
- Create: `frontend/Dockerfile`
- Create: `frontend/.dockerignore`

- [ ] **Step 1: Create `frontend/.dockerignore`** — ignore `node_modules/`, `dist/`, `.git/`.

- [ ] **Step 2: Create `frontend/nginx.conf`** — server block on port 80. Root `/usr/share/nginx/html`. Location `/` → `try_files $uri $uri/ /index.html`. Location `/api/` → `proxy_pass http://backend:8080/api/`, `proxy_set_header Host $host`, `proxy_set_header X-Real-IP $remote_addr`. Location `/uploads/` → `proxy_pass http://backend:8080/uploads/`. Location for assets (css/js/fonts/images) → `expires 1y`, `add_header Cache-Control "public, immutable"`. gzip on, gzip_types text/css application/javascript application/json image/svg+xml.

- [ ] **Step 3: Create `frontend/Dockerfile`** — multi-stage:
  - Stage 1 (build): `FROM node:20-alpine AS build`, `WORKDIR /app`, `COPY package*.json .`, `RUN npm ci`, `COPY . .`, `RUN npm run build`.
  - Stage 2 (run): `FROM nginx:alpine`, `COPY --from=build /app/dist /usr/share/nginx/html`, `COPY nginx.conf /etc/nginx/conf.d/default.conf`, `EXPOSE 80`, `CMD ["nginx", "-g", "daemon off;"]`.

- [ ] **Step 4: Verify** — run `docker build -t lovetree-frontend ./frontend` from project root, confirm image under 30MB.

- [ ] **Step 5: Commit**

```bash
git add frontend/nginx.conf frontend/Dockerfile frontend/.dockerignore
git commit -m "feat: add frontend Dockerfile and nginx config"
```

---

### Task 22: Docker Compose & Environment Config

**Files:**
- Create: `docker-compose.yml`
- Create: `.env.example`

- [ ] **Step 1: Create `docker-compose.yml`** — version '3.8'. Services:

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: lovetree-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: love_tree
      MYSQL_USER: ${MYSQL_USER:-lovetree}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - love-net

  backend:
    build: ./backend
    container_name: lovetree-backend
    restart: unless-stopped
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: love_tree
      DB_USER: ${MYSQL_USER:-lovetree}
      DB_PASSWORD: ${MYSQL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    volumes:
      - uploads:/app/uploads
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - love-net

  nginx:
    build: ./frontend
    container_name: lovetree-nginx
    restart: unless-stopped
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - love-net

volumes:
  mysql_data:
  uploads:

networks:
  love-net:
    driver: bridge
```

- [ ] **Step 2: Create `.env.example`**:

```
MYSQL_ROOT_PASSWORD=change_this_root_password
MYSQL_USER=lovetree
MYSQL_PASSWORD=change_this_user_password
JWT_SECRET=change_this_to_a_random_secret_string
```

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml .env.example
git commit -m "feat: add Docker Compose orchestration and env template"
```

---

### Task 23: README.md

**Files:**
- Create: `README.md`

**Interfaces:**
- Produces: comprehensive README following spec section 9 outline

- [ ] **Step 1: Create `README.md`** — 14 sections per spec:

1. **Project title**: `# 🌸 LoveTree (爱情树)` with description "属于两个人的私密空间，用一棵树记录恋爱的点点滴滴"
2. **Screenshots**: placeholder `<!-- TODO: add screenshots after deployment -->`
3. **Tech stack**: table with Frontend (Vue 3, TypeScript, Pinia, Vue Router, Canvas API), Backend (Spring Boot 3, MyBatis-Plus, JWT), Database (MySQL 8.0), DevOps (Docker, Docker Compose, Nginx)
4. **Features**: bullet list — 🌳 可成长的爱情树, 📜 恋爱事件时间线, 💌 情侣悄悄话, 🖼️ 共享相册, ⏳ 纪念日倒计时, 👫 独立账号+邀请配对, 🎨 梦幻手绘视觉风格
5. **Project structure**: ASCII tree diagram
6. **Prerequisites**: Docker & Docker Compose installed
7. **Quick start**: 4 steps — clone, `cp .env.example .env`, edit secrets, `docker compose up -d`
8. **Environment variables**: table with var name, description, default
9. **API documentation**: endpoint summary table (method, path, auth, description)
10. **Development guide**: how to run frontend (`cd frontend && npm install && npm run dev`), backend (`cd backend && mvn spring-boot:run` with local MySQL), database (manual MySQL setup or Docker mysql only)
11. **Deployment guide**: Docker architecture, Dockerfiles explanation, volumes, network
12. **Architecture diagram**: ASCII art from spec
13. **Database schema**: table definitions (simplified from schema.sql)
14. **License**: MIT

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add comprehensive README with quick start, API docs, and architecture"
```

---

### Task 24: Integration Verification

**Files:**
- None (verification only)

- [ ] **Step 1: Start all services** — `docker compose up -d` from project root. Wait for healthy state (`docker compose ps` shows all "healthy" or "running").

- [ ] **Step 2: Test backend API** — `curl http://localhost/api/auth/register -H "Content-Type: application/json" -d '{"email":"test@example.com","password":"test123","nickname":"小明"}'` → expect 200 with token. `curl http://localhost/api/couple/invite -H "Authorization: Bearer <token>"` → expect 200 with inviteCode.

- [ ] **Step 3: Test frontend** — open `http://localhost` in browser. Verify login page loads with styling. Verify login → invite flow works. Verify dashboard loads with canvas tree drawing.

- [ ] **Step 4: Test photo upload** — upload a test image via `/api/photos`, verify file saved, verify image accessible at `/uploads/...`.

- [ ] **Step 5: Fix any issues found, then commit final fixes.**

```bash
git add -A
git commit -m "chore: integration fixes from full-stack verification"
```

---

### Task 25: Final Cleanup & Git

- [ ] **Step 1: Create `.gitignore`** at project root — ignore `node_modules/`, `dist/`, `target/`, `.env` (but not `.env.example`), `.idea/`, `*.iml`, `.DS_Store`, `uploads/`.

```bash
git add .gitignore
git commit -m "chore: add .gitignore"
```

- [ ] **Step 2: Final review** — run `git log --oneline` to verify clean commit history. Confirm all files committed.

- [ ] **Step 3: Done** 🎉

---

## Implementation Order

Tasks should be executed sequentially: 1→2→3→...→25. Backend dependency chain: 1→2→3→4→5→6→7→8→9. Frontend dependency chain: 10→11→12→13→14→15→16→17→18→19→20→21. Integration: 22→23→24→25. Frontend and backend can be worked in parallel by different agents after Task 3 (shared types defined).

## Estimated Effort

- Backend (Tasks 1-9): ~8-10 hours
- Frontend (Tasks 10-21): ~10-12 hours
- DevOps & Docs (Tasks 22-25): ~2-3 hours
- **Total: ~20-25 hours**
