<template>
  <div class="album-page animate-fade-in-up">
    <div class="album-header">
      <h2 class="album-title">相册</h2>
      <PhotoUploader @uploaded="onPhotoUploaded" />
    </div>

    <div v-if="loading && photos.length === 0" class="state-msg">
      <div class="loading-spinner" />
    </div>

    <div v-else-if="photos.length === 0" class="state-msg">
      <span class="empty-icon">📸</span>
      <p>还没有照片，上传你们的第一张合影吧 📸</p>
    </div>

    <div v-else class="photo-grid">
      <PhotoCard
        v-for="photo in photos"
        :key="photo.id"
        :photo="photo"
        @delete="onPhotoDeleted"
      />
    </div>

    <div v-if="loadingMore" class="loading-more">
      <div class="loading-spinner" />
    </div>

    <div v-if="!hasMore && photos.length > 0" class="end-msg">
      没有更多照片了
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getPhotos } from '@/api/photos'
import type { PhotoResponse } from '@/api/photos'
import PhotoCard from '@/components/PhotoCard.vue'
import PhotoUploader from '@/components/PhotoUploader.vue'

const photos = ref<PhotoResponse[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(true)
const pageSize = 12

async function fetchPhotos() {
  try {
    const res = await getPhotos(page.value, pageSize)
    photos.value.push(...res.records)
    hasMore.value = res.current < res.pages
  } catch (e) {
    console.error('Failed to fetch photos:', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  page.value++
  await fetchPhotos()
  loadingMore.value = false
}

function onPhotoUploaded(photo: PhotoResponse) {
  photos.value.unshift(photo)
}

function onPhotoDeleted(id: number) {
  photos.value = photos.value.filter((p) => p.id !== id)
}

function handleScroll() {
  const scrollY = window.scrollY
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  if (scrollY + windowHeight >= documentHeight - 300) {
    loadMore()
  }
}

onMounted(async () => {
  await fetchPhotos()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.album-page {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
}

.album-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.album-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

@media (max-width: 900px) {
  .photo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .photo-grid {
    grid-template-columns: 1fr;
  }
}

.state-msg {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--color-text);
  opacity: 0.5;
  font-size: 14px;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.6;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-secondary);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.end-msg {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.35;
}
</style>
