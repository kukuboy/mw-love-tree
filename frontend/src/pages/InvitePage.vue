<template>
  <div class="invite-page">
    <!-- Floating petals -->
    <div
      v-for="i in 12"
      :key="i"
      class="petal"
      :style="{
        left: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 10}s`,
        animationDuration: `${6 + Math.random() * 8}s`,
        width: `${8 + Math.random() * 12}px`,
        height: `${8 + Math.random() * 12}px`,
        opacity: 0.3 + Math.random() * 0.4,
      }"
    />

    <div class="invite-container animate-fade-in-up">
      <!-- Create invite card -->
      <div class="invite-card">
        <div class="card-icon">💝</div>
        <h2 class="card-title">创建邀请码</h2>
        <p class="card-desc">生成专属邀请码，发送给另一半</p>

        <div v-if="!inviteCode" class="card-action">
          <button
            class="btn-generate"
            :disabled="generating"
            @click="handleGenerate"
          >
            <span v-if="generating" class="spinner" />
            <span v-else>生成邀请码</span>
          </button>
        </div>

        <div v-else class="code-display">
          <div class="code-box">
            <span class="code-text">{{ inviteCode }}</span>
          </div>
          <button class="btn-copy" @click="handleCopy">
            {{ copied ? '已复制' : '📋 复制邀请码' }}
          </button>
          <p class="code-hint">将上面的邀请码分享给你的另一半</p>
        </div>

        <!-- Error -->
        <div v-if="createError" class="error-msg">{{ createError }}</div>
      </div>

      <!-- Join invite card -->
      <div class="invite-card">
        <div class="card-icon">💕</div>
        <h2 class="card-title">加入对方</h2>
        <p class="card-desc">输入另一半的邀请码，绑定情侣关系</p>

        <div class="join-form">
          <div class="input-group">
            <input
              v-model="joinCode"
              type="text"
              placeholder="请输入邀请码"
              class="input-field"
              :disabled="joining"
            />
          </div>
          <button
            class="btn-join"
            :disabled="joining || !joinCode.trim()"
            @click="handleJoin"
          >
            <span v-if="joining" class="spinner" />
            <span v-else>💕 加入</span>
          </button>
        </div>

        <!-- Error -->
        <div v-if="joinError" class="error-msg">{{ joinError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCoupleStore } from '@/stores/couple'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const coupleStore = useCoupleStore()
const authStore = useAuthStore()

const inviteCode = ref<string | null>(null)
const generating = ref(false)
const createError = ref('')
const copied = ref(false)

const joinCode = ref('')
const joining = ref(false)
const joinError = ref('')

onMounted(async () => {
  // If already paired, redirect to dashboard
  if (authStore.coupleId) {
    router.push('/dashboard')
    return
  }
  // Check if user already has an invite code
  try {
    await coupleStore.fetchCoupleInfo()
    if (coupleStore.inviteCode) {
      inviteCode.value = coupleStore.inviteCode
    }
  } catch {
    // Not paired yet, that's fine
  }
})

async function handleGenerate() {
  createError.value = ''
  generating.value = true
  try {
    await coupleStore.createInvite()
    if (coupleStore.inviteCode) {
      inviteCode.value = coupleStore.inviteCode
    } else {
      createError.value = '生成失败，请重试'
    }
  } catch (e: any) {
    createError.value = e?.response?.data?.message || e?.message || '生成邀请码失败'
  } finally {
    generating.value = false
  }
}

async function handleCopy() {
  if (!inviteCode.value) return
  try {
    await navigator.clipboard.writeText(inviteCode.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch {
    createError.value = '复制失败，请手动复制'
  }
}

async function handleJoin() {
  joinError.value = ''
  if (!joinCode.value.trim()) {
    joinError.value = '请输入邀请码'
    return
  }
  joining.value = true
  try {
    await coupleStore.joinCouple(joinCode.value.trim())
    router.push('/dashboard')
  } catch (e: any) {
    joinError.value = e?.response?.data?.message || e?.message || '加入失败，请检查邀请码'
  } finally {
    joining.value = false
  }
}
</script>

<style scoped>
.invite-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

/* Floating petals */
.petal {
  position: absolute;
  top: -10vh;
  background: radial-gradient(
    ellipse at center,
    rgba(232, 160, 191, 0.6) 0%,
    rgba(243, 204, 213, 0.3) 60%,
    transparent 100%
  );
  border-radius: 50%;
  pointer-events: none;
  animation: petal-fall linear infinite;
}

/* Container */
.invite-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
  max-width: 800px;
  width: 100%;
  position: relative;
  z-index: 1;
}

/* Cards */
.invite-card {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--border-radius);
  box-shadow: 0 8px 32px rgba(232, 160, 191, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 40px 28px 32px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.card-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-accent);
  margin: 0 0 8px;
}

.card-desc {
  font-size: 14px;
  color: var(--color-text);
  opacity: 0.6;
  margin: 0 0 28px;
}

.card-action {
  width: 100%;
}

/* Generate button */
.btn-generate {
  width: 100%;
  max-width: 240px;
  padding: 14px 24px;
  background: linear-gradient(135deg, #E8A0BF, #C5898E);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  font-family: var(--font-family);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

.btn-generate:hover:not(:disabled) {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(232, 160, 191, 0.4);
}

.btn-generate:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-generate:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Code display */
.code-display {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.code-box {
  background: rgba(255, 255, 255, 0.6);
  border: 2px dashed var(--color-primary);
  border-radius: 12px;
  padding: 16px 24px;
  width: 100%;
  max-width: 280px;
  animation: pulse-glow 2s ease-in-out infinite;
}

.code-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-accent);
  letter-spacing: 6px;
  word-break: break-all;
}

.btn-copy {
  padding: 10px 24px;
  background: rgba(232, 160, 191, 0.15);
  border: 1px solid rgba(232, 160, 191, 0.3);
  border-radius: 8px;
  color: var(--color-accent);
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-family);
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.btn-copy:hover {
  background: rgba(232, 160, 191, 0.25);
  transform: scale(1.02);
}

.code-hint {
  font-size: 12px;
  color: var(--color-text);
  opacity: 0.5;
  margin: 0;
}

/* Join form */
.join-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  width: 100%;
}

.input-field {
  width: 100%;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid transparent;
  border-bottom: 2px solid rgba(232, 160, 191, 0.3);
  border-radius: 10px;
  font-size: 16px;
  font-family: var(--font-family);
  color: var(--color-text);
  text-align: center;
  letter-spacing: 3px;
  outline: none;
  transition: border-color 0.3s ease, background 0.3s ease, box-shadow 0.3s ease;
}

.input-field::placeholder {
  color: var(--color-text);
  opacity: 0.35;
  letter-spacing: 0;
}

.input-field:focus {
  border-color: var(--color-primary);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 3px rgba(232, 160, 191, 0.15);
}

.input-field:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-join {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #E8A0BF, #C5898E);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  font-family: var(--font-family);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

.btn-join:hover:not(:disabled) {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(232, 160, 191, 0.4);
}

.btn-join:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-join:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Error message */
.error-msg {
  background: rgba(197, 137, 142, 0.1);
  border: 1px solid rgba(197, 137, 142, 0.3);
  border-radius: 10px;
  padding: 10px 14px;
  margin-top: 20px;
  width: 100%;
  font-size: 13px;
  color: var(--color-accent);
}

/* Spinner */
.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive: stack cards on mobile */
@media (max-width: 640px) {
  .invite-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .invite-card {
    padding: 32px 20px 28px;
  }
}
</style>
