<template>
  <!-- Desktop: vertical sidebar -->
  <nav class="side-nav">
    <div class="nav-items">
      <router-link
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="nav-item"
        :class="{ active: isActive(item.to) }"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </div>
  </nav>

  <!-- Mobile: bottom tab bar -->
  <nav class="mobile-tab-bar">
    <router-link
      v-for="item in navItems"
      :key="item.to"
      :to="item.to"
      class="tab-item"
      :class="{ active: isActive(item.to) }"
    >
      <span class="tab-icon">{{ item.icon }}</span>
      <span class="tab-label">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

interface NavItem {
  icon: string
  label: string
  to: string
}

const navItems: NavItem[] = [
  { icon: '🌳', label: '爱情树', to: '/dashboard' },
  { icon: '📜', label: '时间线', to: '/timeline' },
  { icon: '💌', label: '悄悄话', to: '/messages' },
  { icon: '🖼️', label: '相册', to: '/album' },
  { icon: '⚙️', label: '设置', to: '/settings' },
]

// Check if the current route matches the nav item path
function isActive(path: string): boolean {
  // Exact match for dashboard, prefix match for sub-routes under others
  if (path === '/dashboard') {
    return route.path === '/dashboard'
  }
  // For other routes, match the prefix
  return route.path.startsWith(path)
}
</script>

<style scoped>
/* ---- Desktop sidebar ---- */
.side-nav {
  position: fixed;
  top: 64px;
  left: 0;
  width: 200px;
  height: calc(100vh - 64px);
  z-index: 999;

  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-right: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 2px 0 12px rgba(232, 160, 191, 0.06);

  display: flex;
  flex-direction: column;
  padding: 16px 10px;
  overflow-y: auto;
}

.nav-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--color-text);
  font-size: 15px;
  transition: background 0.25s ease, transform 0.2s ease;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(232, 160, 191, 0.10);
  transform: translateX(4px);
}

.nav-item.active {
  background: rgba(232, 160, 191, 0.20);
  color: var(--color-accent);
  font-weight: 600;
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
  line-height: 1;
}

.nav-label {
  line-height: 1;
}

/* ---- Mobile bottom tab bar ---- */
.mobile-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 64px;
  z-index: 1000;

  display: flex;
  align-items: stretch;
  justify-content: space-around;

  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-top: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 -2px 12px rgba(232, 160, 191, 0.08);

  padding: 4px 0;
  /* Safe area for iOS notch */
  padding-bottom: max(4px, env(safe-area-inset-bottom));
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  text-decoration: none;
  color: var(--color-text);
  opacity: 0.55;
  transition: opacity 0.2s ease, color 0.2s ease;
  border-radius: 8px;
  padding: 4px 0;
  position: relative;
}

.tab-item.active {
  opacity: 1;
  color: var(--color-accent);
}

.tab-item.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--color-primary);
  border-radius: 0 0 3px 3px;
}

.tab-icon {
  font-size: 20px;
  line-height: 1;
}

.tab-label {
  font-size: 10px;
  line-height: 1;
}

/* Responsive: show mobile tab bar on small screens */
@media (min-width: 769px) {
  .mobile-tab-bar {
    display: none;
  }
}

@media (max-width: 768px) {
  .side-nav {
    display: none;
  }
}
</style>
