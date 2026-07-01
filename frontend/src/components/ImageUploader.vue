<template>
  <div class="image-uploader" @dragenter.prevent @dragover.prevent @dragleave.prevent @drop.prevent="handleDrop">
    <!-- Drop zone -->
    <div
      class="drop-zone"
      :class="{ 'drag-over': isDragging, 'has-files': files.length > 0 }"
      @dragenter="isDragging = true"
      @dragover="isDragging = true"
      @dragleave="isDragging = false"
      @drop="isDragging = false"
      @click="openFileDialog"
      role="button"
      tabindex="0"
      @keydown.enter="openFileDialog"
    >
      <div v-if="files.length === 0" class="drop-placeholder">
        <span class="drop-icon">📸</span>
        <span class="drop-text">点击或拖拽图片到此处</span>
        <span class="drop-hint">支持 JPG / PNG / GIF / WebP，每张不超过 10MB</span>
      </div>
      <div v-else class="drop-has-files">
        <span>📸 点击添加更多图片</span>
      </div>

      <input
        ref="fileInputRef"
        type="file"
        accept="image/jpeg,image/png,image/gif,image/webp"
        multiple
        class="file-input"
        @change="handleFileSelect"
      />
    </div>

    <!-- Validation error -->
    <div v-if="error" class="upload-error">{{ error }}</div>

    <!-- Preview grid -->
    <div v-if="files.length > 0" class="preview-grid">
      <div v-for="(file, index) in files" :key="file.name + '-' + index" class="preview-item">
        <img :src="objectUrls[index]" :alt="file.name" class="preview-img" />
        <button
          class="remove-btn"
          @click.stop="removeFile(index)"
          title="移除"
        >
          ✕
        </button>
        <span class="file-size-badge">{{ formatSize(file.size) }}</span>
      </div>
    </div>

    <div v-if="files.length > 0" class="file-count">
      {{ files.length }}/9 张
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount } from 'vue'

const props = defineProps<{
  modelValue: File[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: File[]): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const error = ref('')
const objectUrls = ref<string[]>([])

const files = computed(() => props.modelValue)

watch(files, (newFiles, oldFiles) => {
  // Revoke URLs for removed files
  if (oldFiles) {
    for (const f of oldFiles) {
      if (!newFiles.includes(f)) {
        const idx = oldFiles.indexOf(f)
        if (objectUrls.value[idx]) {
          URL.revokeObjectURL(objectUrls.value[idx])
        }
      }
    }
  }
  // Create URLs for current files
  const urls: string[] = []
  for (const f of newFiles) {
    urls.push(URL.createObjectURL(f))
  }
  objectUrls.value = urls
}, { immediate: true })

onBeforeUnmount(() => {
  for (const url of objectUrls.value) {
    URL.revokeObjectURL(url)
  }
})

const MAX_FILES = 9
const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB

function openFileDialog() {
  fileInputRef.value?.click()
}

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files) {
    addFiles(Array.from(input.files))
  }
  // Reset so same file can be selected again
  input.value = ''
}

const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

function validateFiles(newFiles: File[]): File[] {
  const valid: File[] = []
  for (const file of newFiles) {
    if (!IMAGE_TYPES.includes(file.type)) {
      error.value = `不支持的文件类型：${file.type}`
      return []
    }
    if (file.size > MAX_FILE_SIZE) {
      error.value = `文件 "${file.name}" 超过 10MB 限制`
      return []
    }
    valid.push(file)
  }
  error.value = ''
  return valid
}

function addFiles(newFiles: File[]) {
  const total = files.value.length + newFiles.length
  if (total > MAX_FILES) {
    error.value = `最多只能添加 ${MAX_FILES} 张图片`
    return
  }

  const valid = validateFiles(newFiles)
  if (valid.length === 0) return

  emit('update:modelValue', [...files.value, ...valid])
}

function handleDrop(e: DragEvent) {
  isDragging.value = false
  if (e.dataTransfer?.files) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}

function removeFile(index: number) {
  const newFiles = [...files.value]
  newFiles.splice(index, 1)
  emit('update:modelValue', newFiles)
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.drop-zone {
  position: relative;
  border: 2px dashed var(--color-border);
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s ease;
  background: var(--color-bg-glass);
  min-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  outline: none;
}

.drop-zone:hover {
  border-color: var(--color-primary);
  background: rgba(201, 168, 108, 0.05);
}

.drop-zone.drag-over {
  border-color: var(--color-primary);
  background: rgba(201, 168, 108, 0.1);
  border-style: solid;
  transform: scale(1.01);
}

.drop-zone.has-files {
  border-style: solid;
  border-color: rgba(232, 160, 191, 0.2);
  padding: 16px 20px;
  min-height: auto;
}

.file-input {
  display: none;
}

.drop-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.drop-icon {
  font-size: 36px;
  line-height: 1;
}

.drop-text {
  font-size: 14px;
  color: var(--color-text);
  font-weight: 500;
}

.drop-hint {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.4;
}

.drop-has-files {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.5;
}

.upload-error {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(255, 77, 79, 0.08);
  border-radius: 8px;
  color: #ff4d4f;
  font-size: 13px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.preview-item {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  aspect-ratio: 1;
  background: rgba(0, 0, 0, 0.04);
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.45);
  color: white;
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  line-height: 1;
}

.remove-btn:hover {
  background: rgba(255, 77, 79, 0.85);
}

.file-size-badge {
  position: absolute;
  bottom: 4px;
  left: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 10px;
}

.file-count {
  margin-top: 10px;
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.4;
  text-align: right;
}
</style>
