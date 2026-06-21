<template>
  <div class="photo-uploader">
    <button class="upload-trigger" @click="openFileDialog" :disabled="uploading">
      <span class="trigger-icon">+</span>
      <span class="trigger-text">添加照片</span>
    </button>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="file-input"
      @change="handleFileSelect"
    />

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="selectedFile" class="upload-overlay" @click.self="cancelUpload">
          <div class="upload-dialog">
            <h3 class="upload-title">上传照片</h3>
            <div class="preview-container">
              <img :src="previewUrl" alt="预览" class="preview-img" />
            </div>
            <input
              v-model="caption"
              class="caption-input"
              placeholder="添加描述..."
              maxlength="100"
            />
            <div v-if="uploading" class="progress-bar-wrapper">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: uploadProgress + '%' }" />
              </div>
              <span class="progress-text">{{ uploadProgress }}%</span>
            </div>
            <div class="upload-actions">
              <button class="btn-cancel" @click="cancelUpload" :disabled="uploading">取消</button>
              <button class="btn-upload" @click="startUpload" :disabled="uploading">
                {{ uploading ? '上传中...' : '上传' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue'
import { uploadPhoto } from '@/api/photos'
import type { PhotoResponse } from '@/api/photos'

const emit = defineEmits<{
  (e: 'uploaded', photo: PhotoResponse): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const caption = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)

const previewUrl = computed(() => {
  if (!selectedFile.value) return ''
  return URL.createObjectURL(selectedFile.value)
})

function openFileDialog() {
  fileInputRef.value?.click()
}

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    selectedFile.value = input.files[0]
  }
  input.value = ''
}

function cancelUpload() {
  if (uploading.value) return
  selectedFile.value = null
  caption.value = ''
  uploadProgress.value = 0
}

let progressInterval: ReturnType<typeof setInterval> | null = null

async function startUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  uploadProgress.value = 0

  try {
    progressInterval = setInterval(() => {
      uploadProgress.value = Math.min(uploadProgress.value + 10, 90)
    }, 200)

    const photo = await uploadPhoto(selectedFile.value, caption.value || undefined)

    if (progressInterval) {
      clearInterval(progressInterval)
      progressInterval = null
    }
    uploadProgress.value = 100

    emit('uploaded', photo)
    selectedFile.value = null
    caption.value = ''
    uploadProgress.value = 0
  } catch (e) {
    console.error('Failed to upload photo:', e)
  } finally {
    uploading.value = false
  }
}

onBeforeUnmount(() => {
  if (progressInterval) {
    clearInterval(progressInterval)
  }
})
</script>

<style scoped>
.photo-uploader {
  display: inline-block;
}

.upload-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px dashed var(--color-primary);
  border-radius: 12px;
  background: rgba(232, 160, 191, 0.05);
  color: var(--color-primary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s ease;
  font-family: var(--font-family);
}

.upload-trigger:hover:not(:disabled) {
  background: rgba(232, 160, 191, 0.1);
  border-style: solid;
}

.upload-trigger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.trigger-icon {
  font-size: 20px;
  font-weight: 300;
  line-height: 1;
}

.file-input {
  display: none;
}

.upload-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.upload-dialog {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.upload-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
  text-align: center;
}

.preview-container {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.caption-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text);
  outline: none;
  transition: border-color 0.2s;
  margin-bottom: 12px;
  font-family: var(--font-family);
}

.caption-input:focus {
  border-color: var(--color-primary);
}

.progress-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.6;
  min-width: 36px;
  text-align: right;
}

.upload-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-upload {
  padding: 8px 20px;
  border-radius: 10px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
  font-family: var(--font-family);
}

.btn-cancel {
  background: rgba(0, 0, 0, 0.06);
  color: var(--color-text);
}

.btn-upload {
  background: var(--color-primary);
  color: white;
}

.btn-cancel:hover:not(:disabled),
.btn-upload:hover:not(:disabled) {
  opacity: 0.8;
}

.btn-cancel:disabled,
.btn-upload:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
