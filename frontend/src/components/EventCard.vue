<template>
  <div
    class="event-card"
    @click="goToDetail"
    role="button"
    tabindex="0"
    @keydown.enter="goToDetail"
  >
    <!-- Colored left border by event type -->
    <div class="card-border" :style="{ backgroundColor: borderColor }"></div>

    <div class="card-body">
      <!-- Header: type emoji + label -->
      <div class="card-header">
        <span class="type-emoji">{{ eventTypeEmoji }}</span>
        <span class="type-label">{{ EVENT_TYPE_LABELS[event.eventType] || event.eventType }}</span>
      </div>

      <!-- Title -->
      <div class="card-title">{{ event.title }}</div>

      <!-- Content preview -->
      <div class="card-content">{{ event.content }}</div>

      <!-- Mood emoji -->
      <div v-if="event.mood" class="card-mood">{{ moodEmoji }}</div>

      <!-- Footer -->
      <div class="card-footer">
        <span class="footer-date">{{ formattedDate }}</span>
        <span class="footer-divider">·</span>
        <span class="footer-author">{{ event.authorNickname }}</span>
        <span v-if="imageCount > 0" class="footer-images">
          <span class="footer-divider">·</span>
          📷 {{ imageCount }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface EventData {
  id: number
  coupleId: number
  authorId: number
  authorNickname: string
  title: string
  content: string
  eventType: string
  eventDate: string
  images: string[]
  mood: string
  createdAt: string
}

const props = defineProps<{
  event: EventData
}>()

const router = useRouter()

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

const eventTypeEmoji = computed(() => {
  return EVENT_TYPE_EMOJIS[props.event.eventType] || '📌'
})

const borderColor = computed(() => {
  return EVENT_TYPE_COLORS[props.event.eventType] || '#F5E6CC'
})

const moodEmoji = computed(() => {
  return MOOD_EMOJIS[props.event.mood] || props.event.mood || ''
})

const formattedDate = computed(() => {
  if (!props.event.eventDate) return ''
  const d = new Date(props.event.eventDate)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}年${m}月${day}日`
})

const imageCount = computed(() => {
  return props.event.images?.length || 0
})

function goToDetail() {
  router.push(`/event/${props.event.id}`)
}
</script>

<style scoped>
.event-card {
  display: flex;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(232, 160, 191, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.8);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  outline: none;
}

.event-card:hover,
.event-card:focus-visible {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(232, 160, 191, 0.2);
}

.card-border {
  width: 4px;
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  padding: 16px 18px;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.type-emoji {
  font-size: 18px;
  line-height: 1;
}

.type-label {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.5;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 6px;
  line-height: 1.4;
}

.card-content {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.card-mood {
  margin-top: 8px;
  font-size: 22px;
  line-height: 1;
}

.card-footer {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.4;
}

.footer-divider {
  opacity: 0.3;
}

.footer-images {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
</style>
