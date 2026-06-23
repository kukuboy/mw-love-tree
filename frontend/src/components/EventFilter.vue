<template>
  <div class="event-filter">
    <div class="filter-scroll">
      <button
        v-for="opt in filterOptions"
        :key="opt.value"
        class="filter-chip"
        :class="{ active: modelValue === opt.value }"
        @click="selectFilter(opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const filterOptions = [
  { label: '全部', value: '' },
  { label: '💕初次见面', value: 'first_meet' },
  { label: '💗初次约会', value: 'first_date' },
  { label: '✈️旅行', value: 'travel' },
  { label: '🎂生日', value: 'birthday' },
  { label: '💝纪念日', value: 'anniversary' },
  { label: '📝日常', value: 'daily' },
  { label: '📌其他', value: 'other' },
]

function selectFilter(value: string) {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.event-filter {
  width: 100%;
  margin-bottom: 20px;
}

.filter-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 4px 0;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.filter-scroll::-webkit-scrollbar {
  display: none;
}

.filter-chip {
  flex-shrink: 0;
  padding: 8px 18px;
  border-radius: 20px;
  border: 1.5px solid rgba(232, 160, 191, 0.25);
  background: rgba(255, 255, 255, 0.5);
  color: var(--color-text);
  font-size: 13px;
  font-family: var(--font-family);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  outline: none;
  user-select: none;
}

.filter-chip:hover {
  border-color: var(--color-primary);
  background: rgba(232, 160, 191, 0.08);
}

.filter-chip.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
  box-shadow: 0 2px 12px rgba(232, 160, 191, 0.3);
}
</style>
