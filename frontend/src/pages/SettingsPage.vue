<template>
  <div class="settings-page">
    <!-- Decorative background elements -->
    <div class="bg-layer">
      <div class="bg-gradient"></div>
      <div class="bg-noise"></div>
      <svg class="bg-pattern" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice">
        <defs>
          <pattern id="leaves" patternUnits="userSpaceOnUse" width="120" height="120">
            <path d="M60 10 Q80 30 60 50 Q40 30 60 10" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.15"/>
            <path d="M20 80 Q40 100 20 120 Q0 100 20 80" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.1"/>
            <path d="M100 70 Q120 90 100 110 Q80 90 100 70" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.12"/>
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#leaves)"/>
      </svg>
    </div>

    <!-- Floating botanical decorations -->
    <div class="floating-elements">
      <div class="botanical botanical-1">
        <svg viewBox="0 0 60 100" fill="none">
          <path d="M30 100 Q30 60 30 30" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <ellipse cx="30" cy="25" rx="15" ry="20" fill="currentColor" opacity="0.15"/>
          <ellipse cx="20" cy="40" rx="10" ry="15" fill="currentColor" opacity="0.1" transform="rotate(-30 20 40)"/>
          <ellipse cx="40" cy="50" rx="10" ry="15" fill="currentColor" opacity="0.12" transform="rotate(30 40 50)"/>
        </svg>
      </div>
      <div class="botanical botanical-2">
        <svg viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="30" fill="currentColor" opacity="0.08"/>
          <circle cx="40" cy="40" r="20" fill="currentColor" opacity="0.1"/>
          <circle cx="40" cy="40" r="10" fill="currentColor" opacity="0.15"/>
        </svg>
      </div>
      <div class="botanical botanical-3">
        <svg viewBox="0 0 100 60" fill="none">
          <path d="M10 30 Q50 10 90 30 Q50 50 10 30" fill="currentColor" opacity="0.1"/>
          <path d="M20 30 Q50 15 80 30" stroke="currentColor" stroke-width="0.5" opacity="0.2"/>
        </svg>
      </div>
    </div>

    <!-- Main content -->
    <div class="content-wrapper">
      <!-- Header -->
      <header class="page-header">
        <h1 class="page-title">
          <span class="title-decoration left"></span>
          设置
          <span class="title-decoration right"></span>
        </h1>
        <p class="page-subtitle">守护你们的故事</p>
      </header>

      <!-- Profile Section -->
      <section class="settings-section profile-section">
        <div class="section-card glass-card">
          <div class="card-header">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 20c0-4 4-6 8-6s8 2 8 6"/>
              </svg>
            </div>
            <h2 class="section-title">个人资料</h2>
          </div>

          <div class="profile-content">
            <div class="avatar-container">
              <div class="avatar-wrapper">
                <AvatarUploader v-model="avatarFile" :initial-url="currentAvatarUrl" />
                <div class="avatar-ring"></div>
                <div class="avatar-glow"></div>
              </div>
            </div>

            <div class="form-fields">
              <div class="field-group">
                <label class="field-label">
                  <span class="label-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                  </span>
                  昵称
                </label>
                <div class="input-wrapper">
                  <input
                    v-model="nickname"
                    class="text-input"
                    placeholder="你的专属昵称"
                    maxlength="20"
                  />
                  <div class="input-underline"></div>
                  <span class="char-count">{{ nickname.length }}/20</span>
                </div>
              </div>
            </div>
          </div>

          <button class="action-btn primary-btn" @click="saveProfile" :disabled="savingProfile">
            <span class="btn-text">{{ savingProfile ? '保存中...' : '保存资料' }}</span>
            <span class="btn-glow"></span>
          </button>

          <transition name="message-fade">
            <div v-if="profileMsg" :class="['save-message', profileSuccess ? 'success' : 'error']">
              <span class="message-icon">
                <svg v-if="profileSuccess" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 6L9 17l-5-5"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M15 9l-6 6M9 9l6 6"/>
                </svg>
              </span>
              {{ profileMsg }}
            </div>
          </transition>
        </div>
      </section>

      <!-- Couple Section -->
      <section class="settings-section couple-section">
        <div class="section-card glass-card">
          <CoupleInfo />
        </div>
      </section>

      <!-- Together Date Section -->
      <section class="settings-section date-section">
        <div class="section-card glass-card">
          <div class="card-header">
            <div class="header-icon accent">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </div>
            <h2 class="section-title">在一起日期</h2>
          </div>

          <p class="section-description">
            设置你们真正在一起的日期，系统将自动生成纪念日事件，记录每一个特别的日子
          </p>

          <div class="date-picker-wrapper">
            <div class="date-display" :class="{ 'has-date': togetherDate }">
              <div class="date-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="4" width="18" height="18" rx="2"/>
                  <path d="M16 2v4M8 2v4M3 10h18"/>
                  <path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01M16 18h.01"/>
                </svg>
              </div>
              <DatePickerField
                v-model="togetherDate"
                input-id="together-date"
                name="together-date"
                placeholder="选择日期"
              />
            </div>

            <transition name="message-fade">
              <div v-if="togetherDateMsg" :class="['save-message', togetherDateSuccess ? 'success' : 'error']">
                <span class="message-icon">
                  <svg v-if="togetherDateSuccess" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 6L9 17l-5-5"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M15 9l-6 6M9 9l6 6"/>
                  </svg>
                </span>
                {{ togetherDateMsg }}
              </div>
            </transition>
          </div>

          <button class="action-btn accent-btn" @click="saveTogetherDate" :disabled="savingTogetherDate">
            <span class="btn-text">{{ savingTogetherDate ? '保存中...' : '保存在一起日期' }}</span>
            <span class="btn-glow"></span>
          </button>
        </div>
      </section>

      <!-- Danger Zone -->
      <section class="settings-section danger-section">
        <div class="section-card danger-card">
          <div class="danger-decoration">
            <svg viewBox="0 0 100 100" fill="none">
              <circle cx="50" cy="50" r="45" stroke="currentColor" stroke-width="0.5" stroke-dasharray="4 4"/>
              <path d="M50 20 L50 55 M50 65 L50 80" stroke="currentColor" stroke-width="4" stroke-linecap="round"/>
            </svg>
          </div>

          <div class="danger-content">
            <h3 class="danger-title">
              <span class="danger-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/>
                  <line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
              </span>
              危险区域
            </h3>
            <p class="danger-description">
              解除配对将清空所有数据，此操作不可撤销。请三思而后行。
            </p>
          </div>

          <button class="action-btn danger-btn" @click="showConfirmModal = true">
            <span class="btn-text">解除配对</span>
            <span class="btn-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </span>
          </button>
        </div>
      </section>

      <!-- Footer decoration -->
      <div class="footer-decoration">
        <svg viewBox="0 0 200 20" fill="none">
          <path d="M0 10 Q50 0 100 10 T200 10" stroke="currentColor" stroke-width="0.5" opacity="0.3"/>
        </svg>
      </div>
    </div>

    <!-- Confirm Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showConfirmModal" class="modal-overlay" @click.self="closeConfirmModal">
          <div class="modal-container">
            <div class="modal-decoration top"></div>

            <div class="modal-content">
              <div class="modal-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                </svg>
              </div>

              <h3 class="modal-title">确认解除配对</h3>

              <p class="modal-description">
                此操作不可撤销。你们的所有共同回忆、消息、照片都将被永久删除。
              </p>

              <div class="confirm-input-group">
                <label class="confirm-label">请输入「确认解除」以继续</label>
                <input
                  v-model="confirmText"
                  class="confirm-input"
                  placeholder="确认解除"
                  @keydown.enter="handleUnpair"
                />
              </div>

              <div class="modal-actions">
                <button class="modal-btn cancel-btn" @click="closeConfirmModal">
                  <span>取消</span>
                </button>
                <button
                  class="modal-btn confirm-btn"
                  :disabled="confirmText !== '确认解除'"
                  @click="handleUnpair"
                >
                  <span>确认解除</span>
                </button>
              </div>
            </div>

            <div class="modal-decoration bottom"></div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCoupleStore } from '@/stores/couple'
import { setTogetherDate as setTogetherDateApi } from '@/api/couple'
import { uploadAvatar as uploadAvatarApi } from '@/api/auth'
import AvatarUploader from '@/components/AvatarUploader.vue'
import CoupleInfo from '@/components/CoupleInfo.vue'
import DatePickerField from '@/components/DatePickerField.vue'
import { toDateInputValue } from '@/utils/date'

const authStore = useAuthStore()
const coupleStore = useCoupleStore()

const avatarFile = ref<File | null>(null)
const nickname = ref(authStore.nickname || '')
const savingProfile = ref(false)
const profileMsg = ref('')
const profileSuccess = ref(false)

const togetherDate = ref('')
const savingTogetherDate = ref(false)
const togetherDateMsg = ref('')
const togetherDateSuccess = ref(false)

const showConfirmModal = ref(false)
const confirmText = ref('')

const currentAvatarUrl = computed(() => authStore.avatar || null)

onMounted(async () => {
  await coupleStore.fetchCoupleInfo()
  togetherDate.value = toDateInputValue(coupleStore.partnerInfo?.togetherDate)
})

async function saveProfile() {
  savingProfile.value = true
  profileMsg.value = ''
  try {
    // Upload avatar first if a new file was selected
    if (avatarFile.value) {
      try {
        const avatarUrl = await uploadAvatarApi(avatarFile.value)
        authStore.setAvatar(avatarUrl)
        avatarFile.value = null
        profileSuccess.value = true
        profileMsg.value = '头像已更新'
      } catch (e: any) {
        console.error('Failed to upload avatar:', e)
        profileSuccess.value = false
        profileMsg.value = e?.response?.data?.message || e?.message || '头像上传失败'
        return
      }
    }

    if (nickname.value) {
      authStore.nickname = nickname.value
      localStorage.setItem('nickname', nickname.value)
    }

    if (!profileMsg.value) {
      profileSuccess.value = true
      profileMsg.value = '保存成功'
    }

    setTimeout(() => {
      profileMsg.value = ''
    }, 2000)
  } catch (e) {
    console.error('Failed to save profile:', e)
    profileSuccess.value = false
    profileMsg.value = '保存失败'
  } finally {
    savingProfile.value = false
  }
}

async function saveTogetherDate() {
  if (!togetherDate.value) return
  togetherDateMsg.value = ''
  savingTogetherDate.value = true
  try {
    await setTogetherDateApi(togetherDate.value)
    await coupleStore.fetchCoupleInfo()
    togetherDateSuccess.value = true
    togetherDateMsg.value = '保存成功'
    setTimeout(() => {
      togetherDateMsg.value = ''
    }, 2000)
  } catch (e: any) {
    console.error('Failed to save together date:', e)
    togetherDateSuccess.value = false
    togetherDateMsg.value = e?.response?.data?.message || e?.message || '保存失败，请重试'
  } finally {
    savingTogetherDate.value = false
  }
}

function closeConfirmModal() {
  showConfirmModal.value = false
  confirmText.value = ''
}

function handleUnpair() {
  if (confirmText.value !== '确认解除') return
  authStore.logout()
  closeConfirmModal()
}
</script>

<style scoped>
/* ================================
   CSS Variables - Ethereal Botanical Theme
   ================================ */
:root {
  --color-bg-deep: #1a1f1c;
  --color-bg-surface: #232a26;
  --color-bg-elevated: #2d3630;
  --color-primary: #c9a86c;
  --color-primary-soft: #d4bc94;
  --color-accent: #e8b4b8;
  --color-accent-soft: #f2d4d7;
  --color-danger: #d4716e;
  --color-danger-soft: #e89592;
  --color-text: #e8e4df;
  --color-text-muted: #9a9590;
  --color-text-subtle: #6a6560;
  --color-border: rgba(201, 168, 108, 0.2);
  --color-glow: rgba(201, 168, 108, 0.3);
  --font-display: "Cormorant Garamond", "Noto Serif SC", serif;
  --font-body: "Quicksand", "PingFang SC", sans-serif;
  --border-radius: 20px;
  --border-radius-sm: 12px;
}

/* ================================
   Base Layout
   ================================ */
.settings-page {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding: 40px 20px 60px;
}

/* ================================
   Background Layer
   ================================ */
.bg-layer {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 20% 20%, rgba(201, 168, 108, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse 60% 40% at 80% 80%, rgba(232, 180, 184, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse 100% 100% at 50% 0%, rgba(45, 54, 48, 0.8) 0%, transparent 70%),
    linear-gradient(180deg, #1a1f1c 0%, #232a26 50%, #1a1f1c 100%);
}

.bg-noise {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
}

.bg-pattern {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  color: var(--color-primary);
}

/* ================================
   Floating Botanical Elements
   ================================ */
.floating-elements {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: -1;
}

.botanical {
  position: absolute;
  color: var(--color-primary);
  animation: float 20s ease-in-out infinite;
}

.botanical-1 {
  width: 60px;
  height: 100px;
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.botanical-2 {
  width: 80px;
  height: 80px;
  top: 60%;
  right: 8%;
  animation-delay: -5s;
}

.botanical-3 {
  width: 100px;
  height: 60px;
  bottom: 20%;
  left: 15%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.6; }
  25% { transform: translateY(-15px) rotate(2deg); opacity: 0.8; }
  50% { transform: translateY(-8px) rotate(-1deg); opacity: 0.5; }
  75% { transform: translateY(-20px) rotate(1deg); opacity: 0.7; }
}

/* ================================
   Content Wrapper
   ================================ */
.content-wrapper {
  max-width: 560px;
  margin: 0 auto;
  position: relative;
}

/* ================================
   Header
   ================================ */
.page-header {
  text-align: center;
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease-out;
}

.page-title {
  font-family: var(--font-display);
  font-size: 2.5rem;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.1em;
  margin-bottom: 8px;
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 16px;
}

.title-decoration {
  width: 40px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--color-primary), transparent);
}

.page-subtitle {
  font-family: var(--font-body);
  font-size: 0.95rem;
  color: var(--color-text-muted);
  letter-spacing: 0.2em;
}

/* ================================
   Section Cards
   ================================ */
.section-card {
  position: relative;
  padding: 32px;
  margin-bottom: 24px;
  animation: fadeInUp 0.8s ease-out backwards;
}

.glass-card {
  background: linear-gradient(
    135deg,
    rgba(35, 42, 38, 0.7) 0%,
    rgba(45, 54, 48, 0.6) 100%
  );
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(201, 168, 108, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
}

.header-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.15), rgba(201, 168, 108, 0.05));
  border: 1px solid var(--color-border);
  border-radius: 12px;
  color: var(--color-primary);
}

.header-icon.accent {
  background: linear-gradient(135deg, rgba(232, 180, 184, 0.15), rgba(232, 180, 184, 0.05));
  border-color: rgba(232, 180, 184, 0.2);
  color: var(--color-accent);
}

.header-icon svg {
  width: 22px;
  height: 22px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 1.35rem;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.05em;
}

/* ================================
   Profile Section
   ================================ */
.profile-section { animation-delay: 0.1s; }

.profile-content {
  display: flex;
  gap: 28px;
  margin-bottom: 28px;
}

.avatar-container {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
}

.avatar-ring {
  position: absolute;
  inset: -4px;
  border: 1px solid var(--color-primary);
  border-radius: 50%;
  opacity: 0.5;
  animation: pulse-ring 3s ease-in-out infinite;
}

.avatar-glow {
  position: absolute;
  inset: -12px;
  background: radial-gradient(circle, var(--color-glow) 0%, transparent 70%);
  border-radius: 50%;
  opacity: 0.3;
  animation: pulse-glow 4s ease-in-out infinite;
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.05); opacity: 0.8; }
}

@keyframes pulse-glow {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.1); }
}

.form-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.field-group {
  margin-bottom: 0;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-bottom: 10px;
  letter-spacing: 0.05em;
}

.label-icon {
  width: 16px;
  height: 16px;
  color: var(--color-primary);
}

.label-icon svg {
  width: 100%;
  height: 100%;
}

.input-wrapper {
  position: relative;
}

.text-input {
  width: 100%;
  padding: 14px 0;
  background: transparent;
  border: none;
  border-bottom: 1px solid var(--color-text-subtle);
  font-family: var(--font-body);
  font-size: 1.1rem;
  color: var(--color-text);
  outline: none;
  transition: border-color 0.3s ease;
}

.text-input::placeholder {
  color: var(--color-text-subtle);
}

.text-input:focus {
  border-bottom-color: var(--color-primary);
}

.input-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 1px;
  background: var(--color-primary);
  transition: all 0.4s ease;
  transform: translateX(-50%);
}

.text-input:focus ~ .input-underline {
  width: 100%;
}

.char-count {
  position: absolute;
  right: 0;
  bottom: -22px;
  font-size: 0.75rem;
  color: var(--color-text-subtle);
}

/* ================================
   Action Buttons
   ================================ */
.action-btn {
  position: relative;
  width: 100%;
  padding: 16px 28px;
  border: none;
  border-radius: var(--border-radius-sm);
  font-family: var(--font-body);
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 0.05em;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s ease;
}

.btn-text {
  position: relative;
  z-index: 1;
}

.btn-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.1) 0%,
    transparent 50%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.action-btn:hover:not(:disabled) .btn-glow {
  opacity: 1;
}

.primary-btn {
  background: linear-gradient(135deg, var(--color-primary) 0%, #a88a58 100%);
  color: #1a1f1c;
  box-shadow: 0 4px 20px rgba(201, 168, 108, 0.3);
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(201, 168, 108, 0.4);
}

.accent-btn {
  background: linear-gradient(135deg, var(--color-accent) 0%, #d49a9e 100%);
  color: #1a1f1c;
  box-shadow: 0 4px 20px rgba(232, 180, 184, 0.3);
}

.accent-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(232, 180, 184, 0.4);
}

.danger-btn {
  background: transparent;
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.danger-btn:hover:not(:disabled) {
  background: var(--color-danger);
  color: #fff;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.btn-icon svg {
  width: 100%;
  height: 100%;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ================================
   Couple Section
   ================================ */
.couple-section { animation-delay: 0.2s; }

/* ================================
   Date Section
   ================================ */
.date-section { animation-delay: 0.3s; }

.section-description {
  font-family: var(--font-body);
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.7;
  margin-bottom: 24px;
}

.date-picker-wrapper {
  margin-bottom: 24px;
}

.date-display {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(26, 31, 28, 0.5);
  border: 1px dashed var(--color-text-subtle);
  border-radius: var(--border-radius-sm);
  transition: all 0.3s ease;
}

.date-display.has-date {
  border-style: solid;
  border-color: var(--color-accent);
  background: rgba(232, 180, 184, 0.08);
}

.date-icon {
  width: 22px;
  height: 22px;
  color: var(--color-accent);
}

.date-icon svg {
  width: 100%;
  height: 100%;
}

/* ================================
   Save Message
   ================================ */
.save-message {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  margin-top: 16px;
  border-radius: var(--border-radius-sm);
  font-family: var(--font-body);
  font-size: 0.9rem;
  animation: slideIn 0.3s ease-out;
}

.message-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.message-icon svg {
  width: 100%;
  height: 100%;
}

.save-message.success {
  background: rgba(100, 180, 120, 0.15);
  border: 1px solid rgba(100, 180, 120, 0.3);
  color: #8fd19a;
}

.save-message.error {
  background: rgba(212, 113, 110, 0.15);
  border: 1px solid rgba(212, 113, 110, 0.3);
  color: var(--color-danger-soft);
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-fade-enter-active,
.message-fade-leave-active {
  transition: all 0.3s ease;
}

.message-fade-enter-from,
.message-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ================================
   Danger Section
   ================================ */
.danger-section { animation-delay: 0.4s; }

.danger-card {
  background: linear-gradient(
    135deg,
    rgba(212, 113, 110, 0.08) 0%,
    rgba(35, 42, 38, 0.7) 100%
  );
  border: 1px solid rgba(212, 113, 110, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 36px 32px;
}

.danger-decoration {
  width: 80px;
  height: 80px;
  color: var(--color-danger);
  margin-bottom: 20px;
  opacity: 0.6;
}

.danger-decoration svg {
  width: 100%;
  height: 100%;
}

.danger-content {
  margin-bottom: 24px;
}

.danger-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: var(--font-display);
  font-size: 1.25rem;
  font-weight: 500;
  color: var(--color-danger);
  margin-bottom: 12px;
}

.danger-icon {
  width: 20px;
  height: 20px;
}

.danger-icon svg {
  width: 100%;
  height: 100%;
}

.danger-description {
  font-family: var(--font-body);
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.6;
  max-width: 320px;
}

/* ================================
   Footer Decoration
   ================================ */
.footer-decoration {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  color: var(--color-primary);
  opacity: 0.4;
}

.footer-decoration svg {
  width: 200px;
  height: 20px;
}

/* ================================
   Modal
   ================================ */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(10, 15, 12, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-container {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: linear-gradient(
    180deg,
    rgba(45, 54, 48, 0.98) 0%,
    rgba(35, 42, 38, 0.98) 100%
  );
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 80px rgba(212, 113, 110, 0.1);
  overflow: hidden;
}

.modal-decoration {
  position: absolute;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--color-danger),
    transparent
  );
}

.modal-decoration.top { top: 0; }
.modal-decoration.bottom { bottom: 0; }

.modal-content {
  padding: 40px 32px 36px;
}

.modal-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 113, 110, 0.15);
  border: 1px solid rgba(212, 113, 110, 0.3);
  border-radius: 50%;
  color: var(--color-danger);
}

.modal-icon svg {
  width: 28px;
  height: 28px;
}

.modal-title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 500;
  color: var(--color-text);
  text-align: center;
  margin-bottom: 14px;
}

.modal-description {
  font-family: var(--font-body);
  font-size: 0.9rem;
  color: var(--color-text-muted);
  text-align: center;
  line-height: 1.7;
  margin-bottom: 28px;
}

.confirm-input-group {
  margin-bottom: 28px;
}

.confirm-label {
  display: block;
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-bottom: 12px;
  text-align: center;
}

.confirm-input {
  width: 100%;
  padding: 16px 20px;
  background: rgba(26, 31, 28, 0.6);
  border: 1px solid var(--color-text-subtle);
  border-radius: var(--border-radius-sm);
  font-family: var(--font-body);
  font-size: 1rem;
  color: var(--color-text);
  text-align: center;
  outline: none;
  transition: all 0.3s ease;
}

.confirm-input::placeholder {
  color: var(--color-text-subtle);
}

.confirm-input:focus {
  border-color: var(--color-danger);
  box-shadow: 0 0 20px rgba(212, 113, 110, 0.2);
}

.modal-actions {
  display: flex;
  gap: 14px;
}

.modal-btn {
  flex: 1;
  padding: 14px 20px;
  border: none;
  border-radius: var(--border-radius-sm);
  font-family: var(--font-body);
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: rgba(154, 149, 144, 0.15);
  color: var(--color-text-muted);
}

.cancel-btn:hover {
  background: rgba(154, 149, 144, 0.25);
  color: var(--color-text);
}

.confirm-btn {
  background: var(--color-danger);
  color: #fff;
}

.confirm-btn:hover:not(:disabled) {
  background: var(--color-danger-soft);
}

.confirm-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ================================
   Animations
   ================================ */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Modal Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.4s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

/* ================================
   Responsive
   ================================ */
@media (max-width: 768px) {
  .settings-page {
    padding: 32px 16px 48px;
  }

  .page-title {
    font-size: 2rem;
  }

  .title-decoration {
    width: 30px;
  }

  .section-card {
    padding: 24px;
  }

  .profile-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .form-fields {
    width: 100%;
  }

  .field-group {
    text-align: left;
  }

  .card-header {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.75rem;
    letter-spacing: 0.05em;
  }

  .page-subtitle {
    font-size: 0.85rem;
  }

  .section-card {
    padding: 20px;
    border-radius: 16px;
  }

  .avatar-wrapper {
    width: 80px;
    height: 80px;
  }

  .action-btn {
    padding: 14px 20px;
    font-size: 0.95rem;
  }

  .modal-content {
    padding: 32px 24px 28px;
  }
}
</style>
