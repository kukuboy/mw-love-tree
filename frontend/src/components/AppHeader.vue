<template>
  <header class="app-header">
    <!-- Left: Brand -->
    <div class="header-left">
      <span class="brand-text">🌸 LoveTree</span>
    </div>

    <!-- Center: Days together counter -->
    <div class="header-center">
      <span v-if="daysTogether !== null" class="days-counter">
        在一起 第 <strong>{{ daysTogether }}</strong> 天 ❤️
      </span>
      <span v-else class="days-counter placeholder">--</span>
    </div>

    <!-- Right: User avatar + dropdown -->
    <div class="header-right" ref="avatarRef">
      <button
        class="avatar-btn"
        :style="{ background: avatarGradient }"
        @click="toggleDropdown"
        :title="nickname"
      >
        {{ avatarLetter }}
      </button>

      <Transition name="dropdown">
        <div v-if="showDropdown" class="dropdown-menu">
          <router-link to="/settings" class="dropdown-item" @click="showDropdown = false">
            ⚙️ 设置
          </router-link>
          <div class="dropdown-divider" />
          <button class="dropdown-item dropdown-item--danger" @click="handleLogout">
            🚪 退出登录
          </button>
        </div>
      </Transition>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCoupleStore } from '@/stores/couple'

const router = useRouter()
const authStore = useAuthStore()
const coupleStore = useCoupleStore()

const showDropdown = ref(false)
const avatarRef = ref<HTMLElement | null>(null)

const nickname = computed(() => authStore.nickname || 'U')
const avatarLetter = computed(() => nickname.value.charAt(0).toUpperCase())

// Generate a warm gradient based on the nickname for a consistent color
const avatarGradient = computed(() => {
  const hash = nickname.value
    .split('')
    .reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
  const hue1 = (hash * 13) % 360
  const hue2 = (hash * 27 + 40) % 360
  return `linear-gradient(135deg, hsl(${hue1}, 60%, 70%), hsl(${hue2}, 55%, 65%))`
})

const daysTogether = computed(() => {
  return coupleStore.partnerInfo?.daysTogether ?? null
})

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

function handleLogout() {
  showDropdown.value = false
  authStore.logout()
}

// Close dropdown on outside click
function onDocumentClick(e: MouseEvent) {
  if (avatarRef.value && !avatarRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  z-index: 1000;

  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;

  background: var(--color-bg-glass-strong);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

/* Left — brand */
.header-left {
  flex-shrink: 0;
}

.brand-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-accent);
  letter-spacing: 1px;
}

/* Center — days counter */
.header-center {
  flex: 1;
  text-align: center;
  padding: 0 16px;
}

.days-counter {
  font-size: 15px;
  color: var(--color-text);
  opacity: 0.85;
}

.days-counter strong {
  color: var(--color-accent);
  font-weight: 700;
}

.days-counter.placeholder {
  opacity: 0.3;
}

/* Right — avatar + dropdown */
.header-right {
  position: relative;
  flex-shrink: 0;
}

.avatar-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-family);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.avatar-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 14px rgba(201, 168, 108, 0.3);
}

/* Dropdown */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 150px;
  background: var(--color-bg-glass-strong);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 12px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--color-border);
  padding: 6px;
  z-index: 1001;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-family: var(--font-family);
  color: var(--color-text);
  background: transparent;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s ease;
  white-space: nowrap;
  text-align: left;
}

.dropdown-item:hover {
  background: rgba(201, 168, 108, 0.12);
}

.dropdown-item--danger {
  color: var(--color-danger);
}

.dropdown-item--danger:hover {
  background: rgba(212, 113, 110, 0.12);
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border-soft);
  margin: 4px 8px;
}

/* Dropdown transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* Mobile responsive */
@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }

  .brand-text {
    font-size: 17px;
  }

  .days-counter {
    font-size: 13px;
  }

  .avatar-btn {
    width: 34px;
    height: 34px;
    font-size: 14px;
  }

  .dropdown-menu {
    min-width: 130px;
  }
}
</style>
