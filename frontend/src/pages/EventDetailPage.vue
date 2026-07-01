<template>
  <div class="page-container animate-fade-in-up">
    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-state card">
      <p>{{ error }}</p>
      <button class="back-btn" @click="goBack">返回</button>
    </div>

    <!-- Event detail -->
    <template v-else-if="event">
      <div class="detail-header">
        <button class="back-btn" @click="goBack">← 返回</button>
        <div class="header-actions">
          <button
            v-if="isAuthor"
            class="action-btn edit-btn"
            @click="goToEdit"
          >
            ✏️ 编辑
          </button>
          <button
            v-if="isAuthor"
            class="action-btn delete-btn"
            @click="showDeleteModal = true"
          >
            🗑️ 删除
          </button>
        </div>
      </div>

      <!-- Event type badge -->
      <div class="type-badge" :style="{ backgroundColor: eventTypeColor }">
        <span class="type-badge-emoji">{{ eventTypeEmoji }}</span>
        <span class="type-badge-label">{{ EVENT_TYPE_LABELS[event.eventType] || event.eventType }}</span>
      </div>

      <!-- Title -->
      <h1 class="detail-title">{{ event.title }}</h1>

      <!-- Meta info -->
      <div class="detail-meta">
        <span class="meta-date">{{ formattedDate }}</span>
        <span class="meta-divider">·</span>
        <span class="meta-author">{{ event.authorNickname }}</span>
        <span v-if="event.mood" class="meta-mood">{{ moodEmoji }}</span>
      </div>

      <!-- Content -->
      <div v-if="event.content" class="detail-content card">
        <p class="content-text">{{ event.content }}</p>
      </div>

      <!-- Image gallery -->
      <div v-if="event.images && event.images.length > 0" class="detail-gallery">
        <div
          v-for="(img, idx) in event.images"
          :key="idx"
          class="gallery-item"
          @click="openLightbox(idx)"
        >
          <img :src="img" :alt="'图片 ' + (idx + 1)" class="gallery-img" loading="lazy" />
        </div>
      </div>
    </template>

    <!-- Delete confirmation modal -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
          <div class="modal-card card">
            <div class="modal-icon">🥺</div>
            <p class="modal-text">确定要删除这个回忆吗？<br />树会少一片叶子哦 🥺</p>
            <div class="modal-actions">
              <button class="modal-btn cancel-btn" @click="showDeleteModal = false">
                再想想
              </button>
              <button class="modal-btn confirm-btn" :disabled="deleting" @click="handleDelete">
                {{ deleting ? '删除中...' : '确定删除' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Image lightbox -->
    <ImageLightbox
      :visible="lightboxVisible"
      :images="event?.images || []"
      :current-index="lightboxIndex"
      @close="closeLightbox"
      @update:current-index="lightboxIndex = $event"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEvent, deleteEvent } from '@/api/events'
import { useAuthStore } from '@/stores/auth'
import ImageLightbox from '@/components/ImageLightbox.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const EVENT_TYPE_EMOJIS: Record<string, string> = {
  'first_meet': '💕',
  'first_date': '💗',
  'travel': '✈️',
  'birthday': '🎂',
  'anniversary': '💝',
  'daily': '📝',
  'other': '📌',
}

const EVENT_TYPE_LABELS: Record<string, string> = {
  'first_meet': '初次见面',
  'first_date': '初次约会',
  'travel': '旅行',
  'birthday': '生日',
  'anniversary': '纪念日',
  'daily': '日常',
  'other': '其他',
}

const EVENT_TYPE_COLORS: Record<string, string> = {
  'first_meet': '#FF9AA2',
  'first_date': '#FFB7B2',
  'travel': '#B5EAD7',
  'birthday': '#FFDAC1',
  'anniversary': '#E2F0CB',
  'daily': '#C7CEEA',
  'other': '#F5E6CC',
}

const MOOD_EMOJIS: Record<string, string> = {
  '开心': '😊',
  '幸福': '😍',
  '甜蜜': '🥰',
  '浪漫': '😘',
}

const loading = ref(true)
const error = ref('')
const event = ref<any>(null)
const showDeleteModal = ref(false)
const deleting = ref(false)
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)

const isAuthor = computed(() => {
  return event.value && authStore.userId != null && event.value.authorId === authStore.userId
})

const eventTypeEmoji = computed(() => {
  return EVENT_TYPE_EMOJIS[event.value?.eventType] || '📌'
})

const eventTypeColor = computed(() => {
  return EVENT_TYPE_COLORS[event.value?.eventType] || '#F5E6CC'
})

const moodEmoji = computed(() => {
  const mood = event.value?.mood
  return MOOD_EMOJIS[mood] || mood || ''
})

const formattedDate = computed(() => {
  if (!event.value?.eventDate) return ''
  const d = new Date(event.value.eventDate)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}年${m}月${day}日`
})

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) {
    error.value = '无效的事件 ID'
    loading.value = false
    return
  }
  try {
    const res = await getEvent(id)
    event.value = res as any
  } catch (e: any) {
    console.error('Failed to load event:', e)
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function goBack() {
  router.push('/timeline')
}

function goToEdit() {
  router.push(`/event/new?edit=${event.value.id}`)
}

async function handleDelete() {
  deleting.value = true
  try {
    await deleteEvent(event.value.id)
    router.push('/timeline')
  } catch (e: any) {
    console.error('Failed to delete event:', e)
    error.value = e?.message || '删除失败'
    showDeleteModal.value = false
  } finally {
    deleting.value = false
  }
}

function openLightbox(index: number) {
  lightboxIndex.value = index
  lightboxVisible.value = true
}

function closeLightbox() {
  lightboxVisible.value = false
}
</script>

<style scoped>
.page-container {
  max-width: 680px;
  margin: 0 auto;
  padding: 0 0 60px;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 60px 0;
  color: var(--color-text);
  opacity: 0.4;
  font-size: 14px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-secondary);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  text-align: center;
  padding: 40px 30px;
}

.error-state p {
  color: #ff4d4f;
  font-size: 14px;
  margin-bottom: 16px;
}

/* Header */
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.back-btn {
  padding: 6px 14px;
  border: 1.5px solid var(--color-border);
  border-radius: 18px;
  background: var(--color-bg-glass);
  color: var(--color-text);
  font-size: 13px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  border-color: var(--color-primary);
  background: rgba(201, 168, 108, 0.08);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 18px;
  font-size: 13px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: opacity 0.2s;
}

.edit-btn {
  background: rgba(201, 168, 108, 0.15);
  color: var(--color-primary);
}

.edit-btn:hover {
  opacity: 0.7;
}

.delete-btn {
  background: rgba(212, 113, 110, 0.12);
  color: var(--color-danger-soft);
}

.delete-btn:hover {
  opacity: 0.7;
}

/* Type badge */
.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  margin-bottom: 16px;
}

.type-badge-emoji {
  font-size: 18px;
  line-height: 1;
}

.type-badge-label {
  font-size: 13px;
  color: var(--color-text);
  font-weight: 500;
}

/* Title */
.detail-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.35;
  margin-bottom: 12px;
}

/* Meta */
.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.45;
  margin-bottom: 28px;
}

.meta-divider {
  opacity: 0.3;
}

.meta-mood {
  font-size: 20px;
  line-height: 1;
}

/* Content */
.detail-content {
  padding: 20px 22px;
  margin-bottom: 24px;
}

.content-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-word;
}

/* Gallery */
.detail-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.gallery-item {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 1;
  transition: transform 0.2s;
}

.gallery-item:hover {
  transform: scale(1.02);
}

.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* Delete confirmation modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-card {
  max-width: 360px;
  width: 100%;
  padding: 36px 28px 28px;
  text-align: center;
}

.modal-icon {
  font-size: 44px;
  margin-bottom: 14px;
}

.modal-text {
  font-size: 15px;
  color: var(--color-text);
  line-height: 1.7;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-btn {
  flex: 1;
  padding: 10px 0;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-family);
  cursor: pointer;
  transition: opacity 0.2s;
}

.cancel-btn {
  background: rgba(232, 160, 191, 0.1);
  color: var(--color-text);
}

.cancel-btn:hover {
  opacity: 0.7;
}

.confirm-btn {
  background: linear-gradient(135deg, #ff9aa2, #ff6b6b);
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  opacity: 0.85;
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Modal transition */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
