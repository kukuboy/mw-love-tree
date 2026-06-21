<template>
  <div class="login-page">
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

    <div class="login-card animate-fade-in-up">
      <!-- Branding -->
      <div class="brand">
        <span class="brand-icon">🌸</span>
        <h1 class="brand-title">LoveTree</h1>
        <p class="brand-subtitle">属于我们的爱情树</p>
      </div>

      <!-- Error message -->
      <div v-if="errorMsg" class="error-message">
        {{ errorMsg }}
      </div>

      <!-- Login form -->
      <form @submit.prevent="handleLogin" class="login-form">
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
            placeholder="密码"
            class="input-field"
            :disabled="loading"
            autocomplete="current-password"
            required
          />
        </div>
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading" class="spinner" />
          <span v-else>登录</span>
        </button>
      </form>

      <p class="switch-text">
        还没有账号？
        <router-link to="/register" class="switch-link">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  errorMsg.value = ''
  if (!email.value || !password.value) {
    errorMsg.value = '请填写邮箱和密码'
    return
  }
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/dashboard')
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || e?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
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
.login-card {
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
.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
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
