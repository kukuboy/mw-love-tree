<template>
  <div class="page-container animate-fade-in-up">
    <div class="form-header">
      <router-link to="/timeline" class="back-link">← 返回</router-link>
      <h2 class="page-title">{{ isEdit ? '✏️ 编辑回忆' : '🌸 记录新回忆' }}</h2>
    </div>

    <form class="event-form card" @submit.prevent="handleSubmit">
      <!-- Title -->
      <div class="form-group">
        <label class="form-label">标题</label>
        <input
          v-model="form.title"
          type="text"
          class="form-input"
          placeholder="给这个回忆取个名字吧..."
          required
        />
      </div>

      <!-- Event type -->
      <div class="form-group">
        <label class="form-label">事件类型</label>
        <select v-model="form.eventType" class="form-select" required>
          <option value="" disabled>选择事件类型</option>
          <option value="first_meet">💕 初次见面</option>
          <option value="first_date">💗 初次约会</option>
          <option value="first_kiss">💋 初吻</option>
          <option value="proposal">💍 求婚</option>
          <option value="travel">✈️ 旅行</option>
          <option value="birthday">🎂 生日</option>
          <option value="anniversary">💝 纪念日</option>
          <option value="valentine">🌹 情人节</option>
          <option value="christmas">🎄 圣诞节</option>
          <option value="new_year">🎉 新年</option>
          <option value="daily">📝 日常</option>
          <option value="other">📌 其他</option>
        </select>
      </div>

      <!-- Event date -->
      <div class="form-group">
        <label class="form-label">日期</label>
        <input
          v-model="form.eventDate"
          type="date"
          class="form-input"
          required
        />
      </div>

      <!-- Content -->
      <div class="form-group">
        <label class="form-label">内容</label>
        <textarea
          v-model="form.content"
          class="form-textarea"
          placeholder="写下那些美好的瞬间..."
          rows="5"
        ></textarea>
      </div>

      <!-- Mood -->
      <div class="form-group">
        <label class="form-label">心情</label>
        <MoodPicker v-model="form.mood" />
      </div>

      <!-- Images -->
      <div class="form-group">
        <label class="form-label">图片</label>

        <!-- Existing images (edit mode) -->
        <div v-if="existingImages.length > 0" class="existing-images">
          <span class="existing-label">当前图片：</span>
          <div class="existing-grid">
            <div v-for="(url, idx) in existingImages" :key="idx" class="existing-img-wrapper">
              <img :src="url" alt="" class="existing-img" />
              <button type="button" class="remove-existing-btn" @click="removeExistingImage(idx)">✕</button>
            </div>
          </div>
        </div>

        <ImageUploader v-model="newImageFiles" />
      </div>

      <!-- Error -->
      <div v-if="submitError" class="submit-error">{{ submitError }}</div>

      <!-- Submit -->
      <button
        type="submit"
        class="submit-btn"
        :disabled="submitting"
      >
        <template v-if="submitting">
          <span class="btn-spinner"></span> 提交中...
        </template>
        <template v-else>
          💖 {{ isEdit ? '保存修改' : '添加到爱情树' }}
        </template>
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEvent, createEvent, updateEvent } from '@/api/events'
import { uploadPhoto } from '@/api/photos'
import MoodPicker from '@/components/MoodPicker.vue'
import ImageUploader from '@/components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.edit)
const existingImages = ref<string[]>([])
const newImageFiles = ref<File[]>([])
const submitting = ref(false)
const submitError = ref('')

const form = reactive({
  title: '',
  eventType: '',
  eventDate: '',
  content: '',
  mood: '',
})

onMounted(async () => {
  if (isEdit.value) {
    const eventId = Number(route.query.edit)
    if (!eventId) {
      router.push('/event/new')
      return
    }
    try {
      const event = await getEvent(eventId)
      const data = event as any
      form.title = data.title || ''
      form.eventType = data.eventType || ''
      // Format date for input[type=date]
      if (data.eventDate) {
        const d = new Date(data.eventDate)
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        form.eventDate = `${y}-${m}-${day}`
      }
      form.content = data.content || ''
      form.mood = data.mood || ''
      existingImages.value = data.images || []
    } catch (e: any) {
      console.error('Failed to load event for edit:', e)
      submitError.value = '加载事件失败'
    }
  }
})

async function uploadNewImages(): Promise<string[]> {
  const urls: string[] = []
  for (const file of newImageFiles.value) {
    try {
      const res = await uploadPhoto(file)
      const data = res as any
      if (data.url) {
        urls.push(data.url)
      }
    } catch (e) {
      console.error('Failed to upload image:', e)
      throw new Error('图片上传失败')
    }
  }
  return urls
}

function removeExistingImage(index: number) {
  existingImages.value.splice(index, 1)
}

async function handleSubmit() {
  if (!form.title || !form.eventType || !form.eventDate) {
    submitError.value = '请填写标题、事件类型和日期'
    return
  }

  submitting.value = true
  submitError.value = ''

  try {
    // Upload new images first
    const newUrls = await uploadNewImages()

    // Combine existing + new image URLs
    const allImages = [...existingImages.value, ...newUrls]

    // Prepare event data
    const eventData: any = {
      title: form.title,
      eventType: form.eventType,
      eventDate: form.eventDate,
      content: form.content || '',
      mood: form.mood || '',
      images: allImages,
    }

    let eventId: number

    if (isEdit.value) {
      const id = Number(route.query.edit)
      const res = await updateEvent(id, eventData)
      eventId = id
    } else {
      const res = await createEvent(eventData)
      eventId = (res as any).id
    }

    router.push(`/event/${eventId}`)
  } catch (e: any) {
    console.error('Failed to save event:', e)
    submitError.value = e?.message || '保存失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 0 40px;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.back-link {
  font-size: 14px;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  flex-shrink: 0;
}

.back-link:hover {
  text-decoration: underline;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.event-form {
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid rgba(232, 160, 191, 0.2);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  font-family: var(--font-family);
  color: var(--color-text);
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: var(--color-primary);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  line-height: 1.6;
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='8' viewBox='0 0 12 8' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23C5898E' stroke-width='1.5' fill='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
  cursor: pointer;
}

/* Existing images */
.existing-images {
  margin-bottom: 12px;
}

.existing-label {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.5;
  margin-bottom: 8px;
  display: block;
}

.existing-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.existing-img-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
}

.existing-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-existing-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.45);
  color: white;
  font-size: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.remove-existing-btn:hover {
  background: rgba(255, 77, 79, 0.85);
}

/* Error */
.submit-error {
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(255, 77, 79, 0.08);
  color: #ff4d4f;
  font-size: 13px;
}

/* Submit button */
.submit-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-primary), #D687A8);
  color: white;
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-family);
  cursor: pointer;
  transition: opacity 0.2s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.85;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  vertical-align: middle;
  margin-right: 6px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
