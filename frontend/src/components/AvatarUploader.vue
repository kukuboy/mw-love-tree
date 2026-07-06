<template>
  <div class="avatar-uploader" @click="openFileDialog">
    <div class="avatar-preview">
      <img v-if="previewUrl" :src="previewUrl" alt="头像预览" class="avatar-img" />
      <div v-else class="avatar-placeholder">
        <span class="placeholder-icon">📷</span>
      </div>
      <div class="avatar-overlay">
        <span>点击更换</span>
      </div>
    </div>
    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="file-input"
      @change="handleFileSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount, computed } from 'vue'

const props = defineProps<{
  modelValue: File | null
  initialUrl?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: File | null): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const localPreviewUrl = ref<string | null>(null)

// Show existing remote avatar when no new file is selected
const previewUrl = computed(() => {
  if (localPreviewUrl.value) return localPreviewUrl.value
  if (props.initialUrl) return props.initialUrl
  return null
})

watch(
  () => props.modelValue,
  (newVal) => {
    if (!newVal && localPreviewUrl.value) {
      URL.revokeObjectURL(localPreviewUrl.value)
      localPreviewUrl.value = null
    }
  },
)

function openFileDialog() {
  fileInputRef.value?.click()
}

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const file = input.files[0]

    if (localPreviewUrl.value) {
      URL.revokeObjectURL(localPreviewUrl.value)
    }
    localPreviewUrl.value = URL.createObjectURL(file)
    emit('update:modelValue', file)
  }
  input.value = ''
}

onBeforeUnmount(() => {
  if (localPreviewUrl.value) {
    URL.revokeObjectURL(localPreviewUrl.value)
  }
})
</script>

<style scoped>
.avatar-uploader {
  display: inline-block;
  cursor: pointer;
}

.avatar-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.04);
  border: 3px solid var(--color-secondary);
  transition: border-color 0.2s;
}

.avatar-uploader:hover .avatar-preview {
  border-color: var(--color-primary);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(232, 160, 191, 0.08);
}

.placeholder-icon {
  font-size: 28px;
  opacity: 0.5;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  border-radius: 50%;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.file-input {
  display: none;
}
</style>
