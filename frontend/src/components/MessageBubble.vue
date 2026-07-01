<template>
  <div class="message-bubble-wrapper" :class="{ 'is-mine': isMine }">
    <div class="bubble-container">
      <div class="bubble" :class="{ 'is-mine': isMine }">
        <span class="content">{{ content }}</span>
      </div>
      <span class="timestamp">{{ formattedTime }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  content: string
  isMine: boolean
  createdAt: string
}>()

const formattedTime = computed(() => {
  if (!props.createdAt) return ''
  const date = new Date(props.createdAt)
  if (isNaN(date.getTime())) return ''
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
})
</script>

<style scoped>
.message-bubble-wrapper {
  display: flex;
  margin-bottom: 8px;
  animation: fadeInUp 0.35s ease-out both;
}

.message-bubble-wrapper.is-mine {
  justify-content: flex-end;
}

.bubble-container {
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.bubble {
  position: relative;
  padding: 10px 14px;
  border-radius: 16px;
  word-break: break-word;
  line-height: 1.55;
  font-size: 15px;
  white-space: pre-wrap;
}

/* Partner (left) bubble — accent bg with left tail */
.bubble:not(.is-mine) {
  background: rgba(232, 180, 184, 0.15);
  border: 1px solid rgba(232, 180, 184, 0.2);
  border-bottom-left-radius: 4px;
  align-self: flex-start;
  color: var(--color-text);
}

.bubble:not(.is-mine)::after {
  content: '';
  position: absolute;
  left: -7px;
  bottom: 10px;
  border: 7px solid transparent;
  border-right-color: rgba(232, 180, 184, 0.15);
  border-left: 0;
}

/* Me (right) bubble — primary bg with right tail */
.bubble.is-mine {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border-bottom-right-radius: 4px;
  align-self: flex-end;
  color: var(--color-bg);
}

.bubble.is-mine::after {
  content: '';
  position: absolute;
  right: -7px;
  bottom: 10px;
  border: 7px solid transparent;
  border-left-color: var(--color-primary);
  border-right: 0;
}

.timestamp {
  font-size: 11px;
  color: var(--color-text);
  opacity: 0.45;
  padding: 0 4px;
}

.message-bubble-wrapper.is-mine .timestamp {
  text-align: right;
}
</style>
