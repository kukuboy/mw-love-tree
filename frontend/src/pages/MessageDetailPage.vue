<template>
  <div class="chat-page animate-fade-in-up">
    <!-- Header -->
    <div class="chat-header">
      <button class="back-btn" @click="goBack" aria-label="返回">←</button>
      <img
        v-if="partnerAvatar"
        :src="partnerAvatar"
        :alt="partnerName"
        class="avatar"
      />
      <div v-else class="avatar-placeholder">
        {{ partnerInitial }}
      </div>
      <span class="header-name">{{ partnerName }}</span>
    </div>

    <!-- Message list -->
    <div ref="messageListRef" class="message-list">
      <!-- Loading -->
      <div v-if="loading" class="message-list-status">
        <div class="loading-spinner" />
      </div>

      <!-- Empty -->
      <div v-else-if="messages.length === 0" class="message-list-status empty">
        <span>还没有悄悄话，发送第一条吧～</span>
      </div>

      <!-- Messages -->
      <MessageBubble
        v-for="msg in messages"
        :key="msg.id"
        :content="msg.content"
        :is-mine="msg.senderId === authStore.userId"
        :created-at="msg.createdAt"
      />
    </div>

    <!-- Input footer -->
    <MessageInput :disabled="sending" @send="handleSend" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCoupleStore } from '@/stores/couple'
import {
  getMessages,
  sendMessage as apiSendMessage,
  markRead,
} from '@/api/messages'
import type { MessageResponse } from '@/api/messages'
import MessageBubble from '@/components/MessageBubble.vue'
import MessageInput from '@/components/MessageInput.vue'

const router = useRouter()
const authStore = useAuthStore()
const coupleStore = useCoupleStore()

// ---- Data ----
const messages = ref<MessageResponse[]>([])
const loading = ref(true)
const sending = ref(false)
const messageListRef = ref<HTMLElement | null>(null)

// ---- Computed ----
const partnerAvatar = computed(() => coupleStore.partnerInfo?.partnerAvatar ?? '')
const partnerName = computed(() => coupleStore.partnerInfo?.partnerNickname ?? '对方')
const partnerInitial = computed(() => partnerName.value.charAt(0) || '?')

// ---- Navigation ----
function goBack() {
  router.push('/messages')
}

// ---- Scroll ----
function scrollToBottom(smooth = false) {
  nextTick(() => {
    const el = messageListRef.value
    if (el) {
      el.scrollTo({
        top: el.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto',
      })
    }
  })
}

// ---- Load messages ----
async function loadMessages() {
  try {
    loading.value = true
    const res = await getMessages()
    messages.value = res

    // Mark all unread messages as read
    const unreadIds = res.filter((m) => !m.isRead).map((m) => m.id)
    if (unreadIds.length > 0) {
      await Promise.allSettled(unreadIds.map((id) => markRead(id)))
    }

    scrollToBottom()
  } catch (e) {
    console.error('Failed to load messages:', e)
  } finally {
    loading.value = false
  }
}

// ---- Send message ----
async function handleSend(content: string) {
  if (sending.value) return
  sending.value = true

  // Optimistic add
  const tempId = -Date.now()
  const optimistic: MessageResponse = {
    id: tempId,
    coupleId: 0,
    senderId: authStore.userId!,
    senderNickname: authStore.nickname ?? '',
    content,
    isRead: false,
    createdAt: new Date().toISOString(),
  }
  messages.value.push(optimistic)
  scrollToBottom(true)

  try {
    const sent = await apiSendMessage(content)
    const idx = messages.value.findIndex((m) => m.id === tempId)
    if (idx !== -1) {
      messages.value[idx] = sent
    }
  } catch (e) {
    console.error('Failed to send message:', e)
    // Remove the optimistic message on failure
    messages.value = messages.value.filter((m) => m.id !== tempId)
  } finally {
    sending.value = false
  }
}

// ---- Auto-scroll when messages change ----
watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  },
)

// ---- Polling ----
const POLL_INTERVAL = 5000
let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  // Ensure partner info is loaded
  if (!coupleStore.partnerInfo) {
    await coupleStore.fetchCoupleInfo()
  }

  await loadMessages()

  // Poll for new messages
  pollTimer = setInterval(async () => {
    try {
      const res = await getMessages()
      if (res.length !== messages.value.length) {
        messages.value = res
        scrollToBottom(true)
      }
    } catch {
      // Silent fail on poll
    }
  }, POLL_INTERVAL)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px); /* minus AppHeader height */
  max-width: 680px;
  margin: 0 auto;
  position: relative;
}

/* ---- Header ---- */
.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(243, 204, 213, 0.3);
  flex-shrink: 0;
}

.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px 4px 4px 0;
  color: var(--color-text);
  display: flex;
  align-items: center;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--color-text);
  font-weight: 600;
  flex-shrink: 0;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
}

/* ---- Message list ---- */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

/* Status (loading / empty) */
.message-list-status {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.message-list-status.empty {
  color: var(--color-text);
  opacity: 0.5;
  font-size: 15px;
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

/* ---- Responsive ---- */
@media (max-width: 480px) {
  .chat-page {
    height: calc(100vh - 64px);
  }

  .message-list {
    padding: 12px;
  }
}
</style>
