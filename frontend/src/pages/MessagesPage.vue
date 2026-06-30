<template>
  <div class="page-container animate-fade-in-up">
    <div class="page-header">
      <h1 class="page-title">💌 悄悄话</h1>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner" />
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-container card">
      <p>{{ error }}</p>
      <button class="retry-btn" @click="init">重试</button>
    </div>

    <!-- Content -->
    <template v-else>
      <!-- Partner info card -->
      <div class="partner-card card" @click="goToChat" role="button" tabindex="0" @keydown.enter="goToChat">
        <div class="partner-avatar-wrapper">
          <img
            v-if="partnerAvatar"
            :src="partnerAvatar"
            :alt="partnerNickname"
            class="partner-avatar"
          />
          <div v-else class="partner-avatar-placeholder">
            {{ partnerInitial }}
          </div>
        </div>

        <div class="partner-info">
          <div class="partner-nickname">{{ partnerNickname }}</div>
          <div v-if="daysTogether" class="partner-days">
            在一起 {{ daysTogether }} 天
          </div>
        </div>

        <div class="partner-card-right">
          <span v-if="unreadCount > 0" class="unread-badge">
            {{ displayUnread }}
          </span>
          <span class="arrow">→</span>
        </div>
      </div>

      <!-- Action button -->
      <button class="chat-btn" @click="goToChat">
        查看悄悄话
      </button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCoupleStore } from '@/stores/couple'
import { getCoupleInfo } from '@/api/couple'
import type { CoupleInfoResponse as ApiCoupleInfo } from '@/types'
import { getUnreadCount } from '@/api/messages'

const router = useRouter()
const authStore = useAuthStore()
const coupleStore = useCoupleStore()

const loading = ref(true)
const error = ref('')
const partnerId = ref<number | null>(null)
const unreadCount = ref(0)

// ---- Computed from store ----
const partnerAvatar = computed(() => coupleStore.partnerInfo?.partnerAvatar ?? '')
const partnerNickname = computed(() => coupleStore.partnerInfo?.partnerNickname ?? '对方')
const daysTogether = computed(() => coupleStore.partnerInfo?.daysTogether ?? 0)
const partnerInitial = computed(() => partnerNickname.value.charAt(0) || '?')
const displayUnread = computed(() => (unreadCount.value > 99 ? '99+' : String(unreadCount.value)))

// ---- Init ----
async function init() {
  loading.value = true
  error.value = ''
  try {
    // Ensure couple info is loaded from store
    if (!coupleStore.partnerInfo) {
      await coupleStore.fetchCoupleInfo()
    }

    // Fetch raw couple info to derive the partner's user ID
    const rawInfo: ApiCoupleInfo = await getCoupleInfo()
    partnerId.value = rawInfo.partnerId

    // Fetch unread count (non-critical — swallow errors)
    try {
      unreadCount.value = await getUnreadCount()
    } catch {
      // keep 0
    }
  } catch (e) {
    console.error('Failed to load messages page:', e)
    error.value = '加载失败，请重试'
  } finally {
    loading.value = false
  }
}

// ---- Navigation ----
function goToChat() {
  if (partnerId.value) {
    router.push(`/messages/${partnerId.value}`)
  }
}

onMounted(() => {
  init()
})
</script>

<style scoped>
.page-container {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
}

/* ---- Loading ---- */
.loading-container {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-secondary);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ---- Error ---- */
.error-container {
  padding: 40px 24px;
  text-align: center;
}

.retry-btn {
  margin-top: 12px;
  padding: 8px 20px;
  border-radius: 18px;
  border: 1px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.retry-btn:hover {
  background: var(--color-primary);
  color: var(--color-bg);
}

/* ---- Partner card ---- */
.partner-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.partner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(201, 168, 108, 0.15);
}

.partner-avatar-wrapper {
  flex-shrink: 0;
}

.partner-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
}

.partner-avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--color-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--color-text);
  font-weight: 600;
}

.partner-info {
  flex: 1;
  min-width: 0;
}

.partner-nickname {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 4px;
}

.partner-days {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.55;
}

.partner-card-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.unread-badge {
  background: var(--color-accent);
  color: white;
  font-size: 12px;
  font-weight: 600;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  padding: 0 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow {
  font-size: 18px;
  opacity: 0.4;
}

/* ---- Chat button ---- */
.chat-btn {
  display: block;
  width: 100%;
  margin-top: 16px;
  padding: 14px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-bg);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(201, 168, 108, 0.3);
}

/* ---- Responsive ---- */
@media (max-width: 480px) {
  .page-container {
    padding: 16px;
  }

  .partner-card {
    padding: 16px;
  }

  .partner-avatar,
  .partner-avatar-placeholder {
    width: 48px;
    height: 48px;
  }

  .partner-avatar-placeholder {
    font-size: 18px;
  }
}
</style>
