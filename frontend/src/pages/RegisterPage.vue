<template>
  <div class="register-page">
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

    <div class="register-card animate-fade-in-up">
      <!-- Branding -->
      <div class="brand">
        <span class="brand-icon">🌸</span>
        <h1 class="brand-title">LoveTree</h1>
        <p class="brand-subtitle">创建你们的爱情故事</p>
      </div>

      <!-- Error message -->
      <div v-if="errorMsg" class="error-message">
        {{ errorMsg }}
      </div>

      <!-- Register form -->
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="input-group">
          <input
            v-model="nickname"
            type="text"
            placeholder="昵称"
            class="input-field"
            :disabled="loading"
            autocomplete="nickname"
            required
          />
        </div>
        <div class="input-group">
          <input
            v-model="email"
            type="email"
            placeholder="邮箱"
            class="input-field"
            :disabled="loading"
            autocomplete="email"
            required
          />
        </div>
        <div class="input-group">
          <input
            v-model="password"
            type="password"
            placeholder="密码（至少6位）"
            class="input-field"
            :disabled="loading"
            autocomplete="new-password"
            required
          />
        </div>
        <div class="input-group">
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="确认密码"
            class="input-field"
            :disabled="loading"
            autocomplete="new-password"
            :class="{ 'input-error': passwordMismatch }"
            required
          />
          <p v-if="passwordMismatch" class="field-error">两次密码输入不一致</p>
        </div>
        <button type="submit" class="btn-submit" :disabled="loading || passwordMismatch">
          <span v-if="loading" class="spinner" />
          <span v-else>注册</span>
        </button>
      </form>

      <p class="switch-text">
        已有账号？
        <router-link to="/login" class="switch-link">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const nickname = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')

const passwordMismatch = computed(() => {
  if (!confirmPassword.value) return false
  return password.value !== confirmPassword.value
})

async function handleRegister() {
  errorMsg.value = ''
  if (!nickname.value || !email.value || !password.value || !confirmPassword.value) {
    errorMsg.value = '请填写所有字段'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码至少需要6位'
    return
  }
  if (passwordMismatch.value) {
    errorMsg.value = '两次密码输入不一致'
    return
  }
  loading.value = true
  try {
    await authStore.register(email.value, password.value, nickname.value)
    if (!authStore.coupleId) {
      router.push('/invite')
    } else {
      router.push('/dashboard')
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || e?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 20px;
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

/* Card */
.register-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--border-radius);
  box-shadow: 0 8px 32px rgba(232, 160, 191, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 48px 36px 36px;
  position: relative;
  z-index: 1;
}

/* Branding */
.brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  font-size: 40px;
  display: block;
  margin-bottom: 8px;
}

.brand-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-accent);
  text-shadow: 0 2px 12px rgba(197, 137, 142, 0.3);
  margin: 0 0 6px;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 15px;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0;
}

/* Error message */
.error-message {
  background: rgba(197, 137, 142, 0.1);
  border: 1px solid rgba(197, 137, 142, 0.3);
  border-radius: 10px;
  padding: 10px 14px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--color-accent);
  text-align: center;
}

/* Form */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  position: relative;
}

.input-field {
  width: 100%;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid transparent;
  border-bottom: 2px solid rgba(232, 160, 191, 0.3);
  border-radius: 10px;
  font-size: 15px;
  font-family: var(--font-family);
  color: var(--color-text);
  outline: none;
  transition: border-color 0.3s ease, background 0.3s ease, box-shadow 0.3s ease;
}

.input-field::placeholder {
  color: var(--color-text);
  opacity: 0.4;
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

.input-error {
  border-bottom-color: var(--color-accent) !important;
}

.field-error {
  color: var(--color-accent);
  font-size: 12px;
  margin-top: 4px;
  padding-left: 4px;
}

/* Submit button */
.btn-submit {
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
  margin-top: 4px;
}

.btn-submit:hover:not(:disabled) {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(232, 160, 191, 0.4);
}

.btn-submit:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Spinner */
.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Switch text */
.switch-text {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--color-text);
  opacity: 0.6;
}

.switch-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
  transition: opacity 0.2s;
}

.switch-link:hover {
  opacity: 0.8;
  text-decoration: underline;
}
</style>
