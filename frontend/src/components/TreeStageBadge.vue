<template>
  <div class="stage-badge" :class="{ 'is-blooming': isBlooming }">
    <span class="stage-emoji">{{ stageEmoji }}</span>
    <span class="stage-label">{{ stageLabel }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  stage: string
  stageLabel: string
}>()

const stageEmoji = computed(() => {
  switch (props.stage) {
    case 'SEED':    return '🌱'
    case 'SPROUT':  return '🌿'
    case 'SAPLING': return '🌲'
    case 'BLOOM':   return '🌸'
    case 'FRUIT':   return '🍎'
    default:        return '🌱'
  }
})

const isBlooming = computed(() =>
  props.stage === 'BLOOM' || props.stage === 'FRUIT',
)
</script>

<style scoped>
.stage-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 100px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 12px rgba(232, 160, 191, 0.15);
  font-size: 14px;
  color: var(--color-text);
  white-space: nowrap;
  user-select: none;
  transition: box-shadow 0.3s ease;
}

.stage-badge.is-blooming {
  animation: pulse-glow 2.5s ease-in-out infinite;
}

.stage-emoji {
  font-size: 20px;
  line-height: 1;
}

.stage-label {
  font-weight: 600;
  letter-spacing: 0.5px;
}
</style>
