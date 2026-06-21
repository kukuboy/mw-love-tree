<template>
  <div class="photo-card" @click="openImage">
    <img :src="photo.url" :alt="photo.caption || '照片'" class="photo-img" loading="lazy" />
    <div v-if="photo.caption" class="photo-overlay">
      <span class="photo-caption">{{ photo.caption }}</span>
    </div>
    <button class="delete-btn" @click.stop="handleDelete" title="删除">✕</button>
  </div>
</template>

<script setup lang="ts">
import { deletePhoto } from '@/api/photos'
import type { PhotoResponse } from '@/api/photos'

const props = defineProps<{
  photo: PhotoResponse
}>()

const emit = defineEmits<{
  (e: 'delete', id: number): void
}>()

function openImage() {
  window.open(props.photo.url, '_blank')
}

async function handleDelete() {
  try {
    await deletePhoto(props.photo.id)
    emit('delete', props.photo.id)
  } catch (e) {
    console.error('Failed to delete photo:', e)
  }
}
</script>

<style scoped>
.photo-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 1;
  background: rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.photo-card:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.photo-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  padding: 12px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.photo-card:hover .photo-overlay {
  opacity: 1;
}

.photo-caption {
  color: white;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.45);
  color: white;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease, background 0.2s ease;
  line-height: 1;
  z-index: 2;
}

.photo-card:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(255, 77, 79, 0.85);
}
</style>
