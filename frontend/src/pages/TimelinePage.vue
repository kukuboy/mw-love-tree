<template>
  <div class="page-container animate-fade-in-up">
    <div class="timeline-header">
      <h2 class="page-title">🌳 爱情时间线</h2>
      <router-link to="/event/new" class="add-btn">+ 新回忆</router-link>
    </div>

    <!-- Filter -->
    <EventFilter v-model="filterType" />

    <!-- Loading skeleton -->
    <div v-if="loading && events.length === 0" class="skeleton-list">
      <div v-for="n in 3" :key="n" class="skeleton-card card">
        <div class="skeleton-border"></div>
        <div class="skeleton-body">
          <div class="skeleton-line skeleton-header"></div>
          <div class="skeleton-line skeleton-title"></div>
          <div class="skeleton-line skeleton-text"></div>
          <div class="skeleton-line skeleton-text short"></div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="events.length === 0 && !loading" class="empty-state card">
      <div class="empty-icon">🌱</div>
      <p class="empty-text">还没有一起的记录，去添加第一个吧～</p>
      <router-link to="/event/new" class="empty-btn">添加第一个回忆</router-link>
    </div>

    <!-- Timeline -->
    <div v-else class="timeline-container">
      <div class="timeline-line"></div>

      <div
        v-for="(event, index) in events"
        :key="event.id"
        class="timeline-item"
        :class="{ 'animate-fade-in-up': true }"
        :style="{ animationDelay: (index * 50) + 'ms' }"
      >
        <div class="timeline-dot" :style="{ backgroundColor: eventTypeColor(event.eventType) }"></div>
        <EventCard :event="event" />
      </div>
    </div>

    <!-- Loading more spinner -->
    <div v-if="loading && events.length > 0" class="loading-more">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- No more -->
    <div v-if="!hasMore && events.length > 0 && !loading" class="no-more">
      — 已加载全部记录 —
    </div>

    <!-- Error -->
    <div v-if="error && events.length === 0" class="error-state card">
      <p>{{ error }}</p>
      <button class="retry-btn" @click="resetAndLoad">重新加载</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { getEvents } from '@/api/events'
import EventCard from '@/components/EventCard.vue'
import EventFilter from '@/components/EventFilter.vue'

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

const EVENT_TYPE_COLORS: Record<string, string> = {
  'first_meet': '#FF9AA2',
  'first_date': '#FFB7B2',
  'first_kiss': '#F8B4C8',
  'proposal': '#E8B4D8',
  'travel': '#B5EAD7',
  'birthday': '#FFDAC1',
  'anniversary': '#E2F0CB',
  'valentine': '#F4A0B8',
  'christmas': '#C1E1C1',
  'new_year': '#FFD700',
  'daily': '#C7CEEA',
  'other': '#F5E6CC',
}

const loading = ref(false)
const error = ref('')
const events = ref<EventData[]>([])
const page = ref(1)
const hasMore = ref(true)
const filterType = ref('')

const PAGE_SIZE = 20

async function loadEvents() {
  if (loading.value || !hasMore.value) return
  loading.value = true
  error.value = ''
  try {
    const params: { page: number; size: number; eventType?: string } = {
      page: page.value,
      size: PAGE_SIZE,
    }
    if (filterType.value) {
      params.eventType = filterType.value
    }
    const res = await getEvents(params)
    const records = (res.records || []) as unknown as EventData[]
    events.value.push(...records)
    hasMore.value = res.current < res.pages
    page.value++
  } catch (e: any) {
    console.error('Failed to load events:', e)
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function resetAndLoad() {
  events.value = []
  page.value = 1
  hasMore.value = true
  error.value = ''
  loadEvents()
}

function eventTypeColor(type?: string): string {
  return (type && EVENT_TYPE_COLORS[type]) || '#F5E6CC'
}

// Handle scroll to bottom
function handleScroll() {
  const scrollHeight = document.documentElement.scrollHeight
  const scrollTop = window.scrollY
  const clientHeight = window.innerHeight
  if (scrollHeight - scrollTop - clientHeight < 300) {
    loadEvents()
  }
}

// Watch filter changes -> reset and reload
watch(filterType, () => {
  events.value = []
  page.value = 1
  hasMore.value = true
  error.value = ''
  loadEvents()
})

onMounted(() => {
  loadEvents()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.page-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 0 40px;
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.add-btn {
  padding: 8px 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--color-primary), #D687A8);
  color: white;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s;
  font-family: var(--font-family);
}

.add-btn:hover {
  opacity: 0.85;
}

/* ---------- Skeleton ---------- */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-card {
  display: flex;
  overflow: hidden;
  padding: 0;
}

.skeleton-border {
  width: 4px;
  background: rgba(232, 160, 191, 0.15);
  flex-shrink: 0;
}

.skeleton-body {
  flex: 1;
  padding: 16px 18px;
}

.skeleton-line {
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(232, 160, 191, 0.08) 0%, rgba(232, 160, 191, 0.2) 50%, rgba(232, 160, 191, 0.08) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: 8px;
}

.skeleton-header {
  width: 30%;
  height: 12px;
}

.skeleton-title {
  width: 60%;
  height: 16px;
}

.skeleton-text {
  width: 100%;
}

.skeleton-text.short {
  width: 45%;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ---------- Empty state ---------- */
.empty-state {
  text-align: center;
  padding: 60px 30px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  color: var(--color-text);
  opacity: 0.5;
  margin-bottom: 24px;
  line-height: 1.6;
}

.empty-btn {
  display: inline-block;
  padding: 12px 28px;
  border-radius: 24px;
  background: linear-gradient(135deg, var(--color-primary), #D687A8);
  color: white;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s;
  font-family: var(--font-family);
}

.empty-btn:hover {
  opacity: 0.85;
}

/* ---------- Timeline ---------- */
.timeline-container {
  position: relative;
  padding-left: 28px;
}

.timeline-line {
  position: absolute;
  left: 12px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: linear-gradient(to bottom, var(--color-secondary), var(--color-primary), var(--color-secondary));
  border-radius: 1px;
}

.timeline-item {
  position: relative;
  margin-bottom: 16px;
}

.timeline-dot {
  position: absolute;
  left: -20px;
  top: 22px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--color-primary);
  border: 3px solid white;
  box-shadow: 0 0 0 2px rgba(232, 160, 191, 0.3);
  z-index: 1;
}

/* ---------- Loading more ---------- */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  color: var(--color-text);
  opacity: 0.4;
  font-size: 13px;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-secondary);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ---------- No more ---------- */
.no-more {
  text-align: center;
  padding: 28px;
  color: var(--color-text);
  opacity: 0.3;
  font-size: 13px;
}

/* ---------- Error state ---------- */
.error-state {
  text-align: center;
  padding: 40px 30px;
}

.error-state p {
  color: #ff4d4f;
  font-size: 14px;
  margin-bottom: 16px;
}

.retry-btn {
  padding: 10px 24px;
  border-radius: 20px;
  border: 1.5px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: var(--font-family);
  -webkit-tap-highlight-color: transparent;
}

.retry-btn:hover {
  background: var(--color-primary);
  color: white;
}

/* ---- Mobile responsive ---- */
@media (max-width: 768px) {
  .page-container {
    padding: 0 0 32px;
  }

  .timeline-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
  }

  .add-btn {
    padding: 8px 14px;
    font-size: 12px;
  }

  .timeline-container {
    padding-left: 24px;
  }

  .timeline-dot {
    width: 12px;
    height: 12px;
    left: -18px;
    top: 20px;
  }

  .empty-state {
    padding: 40px 20px;
  }

  .empty-icon {
    font-size: 36px;
  }
}

@media (max-width: 480px) {
  .timeline-container {
    padding-left: 20px;
  }

  .timeline-dot {
    left: -16px;
    width: 10px;
    height: 10px;
  }

  .timeline-line {
    left: 9px;
  }
}
</style>
