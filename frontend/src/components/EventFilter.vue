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
  { label: '💋初吻', value: 'first_kiss' },
  { label: '💍求婚', value: 'proposal' },
  { label: '✈️旅行', value: 'travel' },
  { label: '🎂生日', value: 'birthday' },
  { label: '💝纪念日', value: 'anniversary' },
  { label: '🌹情人节', value: 'valentine' },
  { label: '🎄圣诞节', value: 'christmas' },
  { label: '🎉新年', value: 'new_year' },
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
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-glass);
  color: var(--color-text);
  font-size: 13px;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  outline: none;
  user-select: none;
}

.filter-chip:hover {
  border-color: var(--color-primary);
  background: rgba(201, 168, 108, 0.1);
}

.filter-chip.active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-bg);
  border-color: var(--color-primary);
  box-shadow: 0 2px 12px rgba(201, 168, 108, 0.3);
}

/* ---- Mobile responsive ---- */
@media (max-width: 768px) {
  .filter-scroll {
    gap: 8px;
    padding: 4px 0 8px;
  }

  .filter-chip {
    padding: 6px 14px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .filter-chip {
    padding: 6px 12px;
    font-size: 11px;
  }
}
</style>
