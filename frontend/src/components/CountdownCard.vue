<template>
  <div class="countdown-card card">
    <div class="card-header">
      <span class="card-title">下一个特殊日子</span>
    </div>
    <div class="card-body">
      <template v-if="daysUntil === 0">
        <div class="celebration">🎉 就是今天！</div>
        <div class="event-name">{{ name }}</div>
      </template>
      <template v-else>
        <div class="days-number">{{ daysUntil }}</div>
        <div class="days-unit">天后</div>
        <div class="event-name">{{ name }}</div>
        <div class="event-date">{{ formattedDate }}</div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  name: string
  date: string
  daysUntil: number
}>()

const formattedDate = computed(() => {
  if (!props.date) return ''
  try {
    const d = new Date(props.date)
    if (isNaN(d.getTime())) return props.date
    return `${d.getMonth() + 1}月${d.getDate()}日`
  } catch {
    return props.date
  }
})
</script>

<style scoped>
.countdown-card {
  padding: 20px 16px;
  text-align: center;
}

.card-header {
  margin-bottom: 12px;
}

.card-title {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.55;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.days-number {
  font-size: 56px;
  font-weight: 700;
  color: var(--color-accent);
  line-height: 1;
  font-family: 'Noto Serif SC', serif;
  letter-spacing: -1px;
}

.days-unit {
  font-size: 16px;
  color: var(--color-text);
  opacity: 0.6;
  margin-bottom: 6px;
}

.event-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.event-date {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.4;
  margin-top: 2px;
}

.celebration {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-accent);
  margin-bottom: 8px;
  animation: pulse-glow 1.5s ease-in-out infinite;
}

@media (max-width: 768px) {
  .days-number {
    font-size: 42px;
  }

  .celebration {
    font-size: 18px;
  }
}
</style>
