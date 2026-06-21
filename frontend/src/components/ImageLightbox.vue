<template>
  <Teleport to="body">
    <Transition name="lightbox-fade">
      <div
        v-if="visible"
        class="lightbox-overlay"
        @click.self="close"
        @keydown.escape="close"
        tabindex="0"
        ref="overlayRef"
      >
        <!-- Close button -->
        <button class="lightbox-close" @click="close" title="关闭">✕</button>

        <!-- Previous button -->
        <button
          v-if="images.length > 1"
          class="lightbox-nav lightbox-prev"
          @click="prev"
          title="上一张"
        >
          ‹
        </button>

        <!-- Current image -->
        <div class="lightbox-image-wrapper">
          <img
            :src="currentImage"
            :alt="'图片 ' + (currentIndex + 1)"
            class="lightbox-image"
            @click.stop
          />
        </div>

        <!-- Next button -->
        <button
          v-if="images.length > 1"
          class="lightbox-nav lightbox-next"
          @click="next"
          title="下一张"
        >
          ›
        </button>

        <!-- Counter -->
        <div v-if="images.length > 1" class="lightbox-counter">
          {{ currentIndex + 1 }} / {{ images.length }}
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps<{
  visible: boolean
  images: string[]
  currentIndex: number
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:currentIndex', value: number): void
}>()

const overlayRef = ref<HTMLDivElement | null>(null)

const currentImage = computed(() => {
  return props.images[props.currentIndex] || ''
})

function close() {
  emit('close')
}

function prev() {
  if (props.images.length <= 1) return
  const idx = props.currentIndex <= 0 ? props.images.length - 1 : props.currentIndex - 1
  emit('update:currentIndex', idx)
}

function next() {
  if (props.images.length <= 1) return
  const idx = props.currentIndex >= props.images.length - 1 ? 0 : props.currentIndex + 1
  emit('update:currentIndex', idx)
}

function handleKeydown(e: KeyboardEvent) {
  if (!props.visible) return
  if (e.key === 'Escape') {
    close()
  } else if (e.key === 'ArrowLeft') {
    prev()
  } else if (e.key === 'ArrowRight') {
    next()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})

// Focus the overlay when visible for keyboard events
watch(() => props.visible, (val) => {
  if (val) {
    setTimeout(() => {
      overlayRef.value?.focus()
    }, 100)
  }
})
</script>

<style scoped>
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  outline: none;
  padding: 40px;
}

.lightbox-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  z-index: 2;
}

.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.lightbox-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: white;
  font-size: 30px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  z-index: 2;
  line-height: 1;
  user-select: none;
}

.lightbox-nav:hover {
  background: rgba(255, 255, 255, 0.25);
}

.lightbox-prev {
  left: 20px;
}

.lightbox-next {
  right: 20px;
}

.lightbox-image-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 90vw;
  max-height: 85vh;
}

.lightbox-image {
  max-width: 100%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4);
  user-select: none;
  -webkit-user-drag: none;
}

.lightbox-counter {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 6px 16px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.4);
  color: white;
  font-size: 14px;
  font-weight: 500;
}

/* Transition */
.lightbox-fade-enter-active,
.lightbox-fade-leave-active {
  transition: opacity 0.3s ease;
}

.lightbox-fade-enter-from,
.lightbox-fade-leave-to {
  opacity: 0;
}
</style>
