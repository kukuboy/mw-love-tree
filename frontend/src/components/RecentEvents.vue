<template>
  <div class="recent-events">
    <div class="section-header">
      <h3 class="section-title">最近记录</h3>
      <router-link to="/timeline" class="view-all">查看全部 &rarr;</router-link>
    </div>

    <div v-if="loading" class="state-msg">加载中...</div>
    <div v-else-if="events.length === 0" class="state-msg">
      还没有记录，快<router-link to="/event/new" class="inline-link">去记录</router-link>吧
    </div>
    <div v-else class="event-list">
      <div
        v-for="event in events"
        :key="event.id"
        class="event-card card"
        @click="goToEvent(event.id)"
        role="button"
        tabindex="0"
        @keydown.enter="goToEvent(event.id)"
      >
        <div class="event-icon">{{ eventTypeEmoji(event.eventType) }}</div>
        <div class="event-info">
          <div class="event-title">{{ event.title }}</div>
          <div class="event-meta">
            <span class="event-date">{{ relativeDate(event.eventDate) }}</span>
            <!--
              mood is optional; the API may or may not include it.
              If present as a string, map it to an emoji.
            -->
            <span v-if="event.mood" class="event-mood">{{ moodEmoji(event.mood) }}</span>
          </div>
        </div>
        <span class="event-arrow">&rsaquo;</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEvents } from '@/api/events'
import type { EventResponse } from '@/api/events'

const router = useRouter()
const events = ref<EventResponse[]>([])
const loading = ref(true)

const EVENT_EMOJIS: Record<string, string> = {
  'first_meet': '💕',
  'first_date': '💗',
  'first_kiss': '💋',
  'proposal': '💍',
  'travel': '✈️',
  'birthday': '🎂',
  'anniversary': '💝',
  'valentine': '🌹',
  'christmas': '🎄',
  'new_year': '🎉',
  'daily': '📝',
  'other': '📌',
}

const MOOD_EMOJIS: Record<string, string> = {
  happy: '😊',
  love: '🥰',
  sad: '😢',
  angry: '😠',
  excited: '🤩',
  grateful: '🙏',
  miss: '🥺',
  romantic: '💕',
}

function eventTypeEmoji(type?: string): string {
  return (type && EVENT_EMOJIS[type]) || '💝'
}

function moodEmoji(mood?: string): string {
  return (mood && MOOD_EMOJIS[mood]) || mood || ''
}

function relativeDate(dateStr: string): string {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const date = new Date(dateStr)
  date.setHours(0, 0, 0, 0)
  const diffDays = Math.round((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
  if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`
  return `${Math.floor(diffDays / 365)}年前`
}

function goToEvent(id: number) {
  router.push(`/event/${id}`)
}

onMounted(async () => {
  try {
    const res = await getEvents({ page: 1, size: 3 })
    events.value = res.records || []
  } catch (e) {
    console.error('Failed to fetch recent events:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.recent-events {
  width: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.view-all {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.2s;
}

.view-all:hover {
  opacity: 0.7;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.event-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  outline: none;
}

.event-card:hover,
.event-card:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(201, 168, 108, 0.15);
}

.event-icon {
  font-size: 22px;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(201, 168, 108, 0.1);
  border-radius: 10px;
}

.event-info {
  flex: 1;
  min-width: 0;
}

.event-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.event-date {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.45;
}

.event-mood {
  font-size: 14px;
  line-height: 1;
}

.event-arrow {
  font-size: 22px;
  color: var(--color-text);
  opacity: 0.25;
  flex-shrink: 0;
  font-weight: 300;
}

.state-msg {
  padding: 28px;
  text-align: center;
  color: var(--color-text);
  opacity: 0.5;
  font-size: 14px;
}

.inline-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.inline-link:hover {
  text-decoration: underline;
}
</style>
