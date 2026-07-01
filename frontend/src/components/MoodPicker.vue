<template>
  <div class="mood-picker">
    <div
      v-for="mood in moods"
      :key="mood.value"
      class="mood-option"
      :class="{ selected: modelValue === mood.value }"
      @click="selectMood(mood.value)"
      role="button"
      tabindex="0"
      @keydown.enter="selectMood(mood.value)"
    >
      <span class="mood-emoji">{{ mood.emoji }}</span>
      <span class="mood-label">{{ mood.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const moods = [
  { emoji: '😊', label: '开心', value: '开心' },
  { emoji: '😍', label: '幸福', value: '幸福' },
  { emoji: '🥰', label: '甜蜜', value: '甜蜜' },
  { emoji: '😘', label: '浪漫', value: '浪漫' },
]

function selectMood(value: string) {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.mood-picker {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.mood-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 20px;
  border-radius: 16px;
  background: var(--color-bg-glass);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
  outline: none;
  min-width: 80px;
}

.mood-option:hover {
  border-color: var(--color-border);
  background: rgba(201, 168, 108, 0.08);
}

.mood-option.selected {
  transform: scale(1.15);
  border-color: var(--color-primary);
  background: rgba(201, 168, 108, 0.15);
  box-shadow: 0 0 20px rgba(201, 168, 108, 0.25);
}

.mood-emoji {
  font-size: 32px;
  line-height: 1;
  transition: transform 0.25s ease;
}

.mood-option.selected .mood-emoji {
  transform: scale(1.1);
}

.mood-label {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.6;
  font-weight: 500;
}

.mood-option.selected .mood-label {
  opacity: 1;
  color: var(--color-primary);
}
</style>
