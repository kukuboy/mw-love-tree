# Task 12: Frontend Router Setup — Report

## Summary

Implemented Vue Router with auth guards and 11 stub page components for the frontend.

## What was done

### Router (`frontend/src/router/index.ts`)
- Replaced the minimal empty-routes stub with a full router configuration
- Added 11 routes: `/login`, `/register`, `/invite`, `/dashboard`, `/timeline`, `/event/new`, `/event/:id`, `/messages`, `/messages/:userId`, `/album`, `/settings`
- Auth guard in `beforeEach`:
  - Checks `localStorage.getItem('token')` for routes with `meta.requiresAuth: true`
  - No token + protected route → redirect to `/login`
  - Has token + visiting `/login` or `/register` → redirect to `/dashboard`
- All page components are lazy-loaded via `() => import(...)`

### Stub Pages (`frontend/src/pages/`)
Created 11 minimal Vue SFC stub files:
- `LoginPage.vue`, `RegisterPage.vue`, `InvitePage.vue`, `DashboardPage.vue`, `TimelinePage.vue`, `EventFormPage.vue`, `EventDetailPage.vue`, `MessagesPage.vue`, `MessageDetailPage.vue`, `AlbumPage.vue`, `SettingsPage.vue`

Each contains a single `<div class="page-container">PageName</div>` with empty `<script setup lang="ts">`.

## Files changed
- **Modified:** `frontend/src/router/index.ts`
- **Created:** `frontend/src/pages/LoginPage.vue`
- **Created:** `frontend/src/pages/RegisterPage.vue`
- **Created:** `frontend/src/pages/InvitePage.vue`
- **Created:** `frontend/src/pages/DashboardPage.vue`
- **Created:** `frontend/src/pages/TimelinePage.vue`
- **Created:** `frontend/src/pages/EventFormPage.vue`
- **Created:** `frontend/src/pages/EventDetailPage.vue`
- **Created:** `frontend/src/pages/MessagesPage.vue`
- **Created:** `frontend/src/pages/MessageDetailPage.vue`
- **Created:** `frontend/src/pages/AlbumPage.vue`
- **Created:** `frontend/src/pages/SettingsPage.vue`
