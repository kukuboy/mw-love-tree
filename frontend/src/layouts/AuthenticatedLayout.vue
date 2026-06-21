<template>
  <div class="authenticated-layout">
    <AppHeader />
    <SideNav />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import SideNav from '@/components/SideNav.vue'
</script>

<style scoped>
.authenticated-layout {
  min-height: 100vh;
  position: relative;
}

.main-content {
  padding-top: 64px;             /* space for fixed header */
  padding-left: 200px;           /* space for desktop sidebar */
  padding-bottom: 0;
  min-height: 100vh;
  transition: padding-left 0.3s ease;
}

/* Mobile: no sidebar offset, add bottom padding for tab bar */
@media (max-width: 768px) {
  .main-content {
    padding-left: 0;
    padding-bottom: 64px;        /* space for mobile tab bar */
    padding-bottom: calc(64px + env(safe-area-inset-bottom));
  }
}
</style>
