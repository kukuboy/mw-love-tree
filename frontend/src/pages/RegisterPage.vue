<template>
  <div class="register-page">
    <!-- Background layers -->
    <div class="bg-layer">
      <div class="bg-gradient"></div>
      <div class="bg-noise"></div>
      <svg class="bg-pattern" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice">
        <defs>
          <pattern id="leaves-register" patternUnits="userSpaceOnUse" width="120" height="120">
            <path d="M60 10 Q80 30 60 50 Q40 30 60 10" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.15"/>
            <path d="M20 80 Q40 100 20 120 Q0 100 20 80" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.1"/>
            <path d="M100 70 Q120 90 100 110 Q80 90 100 70" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.12"/>
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#leaves-register)"/>
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

    <!-- Register Card -->
    <div class="register-card">
      <div class="card-decoration top"></div>

      <!-- Branding -->
      <div class="brand">
        <div class="brand-icon">
          <svg viewBox="0 0 64 64" fill="none">
            <path d="M32 8 C24 16, 16 24, 16 36 C16 46, 22 52, 32 52 C42 52, 48 46, 48 36 C48 24, 40 16, 32 8Z" fill="currentColor" opacity="0.2"/>
            <path d="M32 12 C26 18, 20 26, 20 36 C20 44, 24 48, 32 48 C40 48, 44 44, 44 36 C44 26, 38 18, 32 12Z" fill="currentColor" opacity="0.3"/>
            <path d="M32 52 L32 60" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
          </svg>
        </div>
        <h1 class="brand-title">LoveTree</h1>
        <p class="brand-subtitle">创建你们的爱情故事</p>
      </div>

      <!-- Error message -->
      <transition name="shake">
        <div v-if="errorMsg" class="error-message">
          <span class="error-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </span>
          {{ errorMsg }}
        </div>
      </transition>

      <!-- Register form -->
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <input
            v-model="nickname"
            type="text"
            placeholder="昵称"
            class="input-field"
            :disabled="loading"
            autocomplete="nickname"
            required
          />
          <div class="input-underline"></div>
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
          </div>
          <input
            v-model="email"
            type="email"
            placeholder="邮箱"
            class="input-field"
            :disabled="loading"
            autocomplete="email"
            required
          />
          <div class="input-underline"></div>
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="11" width="18" height="11" rx="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </div>
          <input
            v-model="password"
            type="password"
            placeholder="密码（至少6位）"
            class="input-field"
            :disabled="loading"
            autocomplete="new-password"
            required
          />
          <div class="input-underline"></div>
        </div>

        <div class="input-group" :class="{ 'has-error': passwordMismatch }">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
          </div>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="确认密码"
            class="input-field"
            :disabled="loading"
            autocomplete="new-password"
            required
          />
          <div class="input-underline"></div>
        </div>

        <transition name="fade-slide">
          <p v-if="passwordMismatch" class="field-error">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M15 9l-6 6M9 9l6 6"/>
            </svg>
            两次密码输入不一致
          </p>
        </transition>

        <button type="submit" class="btn-submit" :disabled="loading || passwordMismatch">
          <span v-if="loading" class="spinner" />
          <span v-else class="btn-text">注 册</span>
          <span class="btn-glow"></span>
        </button>
      </form>

      <p class="switch-text">
        已有账号？
        <router-link to="/login" class="switch-link">立即登录</router-link>
      </p>

      <div class="card-decoration bottom"></div>
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
/* ================================
   Ethereal Botanical - Register Theme
   ================================ */
:root {
  --color-primary: #c9a86c;
  --color-primary-dark: #a88a58;
  --color-accent: #e8b4b8;
  --color-bg-deep: #1a1f1c;
  --color-bg-surface: #232a26;
  --color-text: #e8e4df;
  --color-text-muted: #9a9590;
  --color-text-subtle: #6a6560;
  --color-danger: #d4716e;
  --color-border: rgba(201, 168, 108, 0.2);
  --font-display: "Cormorant Garamond", "Noto Serif SC", serif;
  --font-body: "Quicksand", "PingFang SC", sans-serif;
}

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* ================================
   Background
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
   Floating Elements
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
  width: 80px;
  height: 130px;
  top: 10%;
  right: 8%;
  animation-delay: -3s;
}

.botanical-2 {
  width: 100px;
  height: 100px;
  top: 70%;
  left: 10%;
  animation-delay: -10s;
}

.botanical-3 {
  width: 120px;
  height: 70px;
  bottom: 10%;
  right: 12%;
  animation-delay: -6s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.5; }
  25% { transform: translateY(-20px) rotate(3deg); opacity: 0.7; }
  50% { transform: translateY(-10px) rotate(-2deg); opacity: 0.4; }
  75% { transform: translateY(-25px) rotate(2deg); opacity: 0.6; }
}

/* ================================
   Register Card
   ================================ */
.register-card {
  width: 100%;
  max-width: 420px;
  position: relative;
  background: linear-gradient(
    160deg,
    rgba(35, 42, 38, 0.85) 0%,
    rgba(45, 54, 48, 0.75) 50%,
    rgba(35, 42, 38, 0.85) 100%
  );
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  border: 1px solid var(--color-border);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.4),
    0 0 80px rgba(201, 168, 108, 0.05),
    inset 0 1px 0 rgba(201, 168, 108, 0.1);
  padding: 48px 40px 36px;
  animation: cardEnter 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-decoration {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--color-primary),
    transparent
  );
  opacity: 0.6;
}

.card-decoration.top { top: 0; }
.card-decoration.bottom { bottom: 0; }

/* ================================
   Branding
   ================================ */
.brand {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  color: var(--color-primary);
  animation: iconPulse 4s ease-in-out infinite;
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

@keyframes iconPulse {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.05); opacity: 1; }
}

.brand-title {
  font-family: var(--font-display);
  font-size: 2.25rem;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.15em;
  margin: 0 0 8px;
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-accent) 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  letter-spacing: 0.2em;
  margin: 0;
}

/* ================================
   Error Message
   ================================ */
.error-message {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(212, 113, 110, 0.12);
  border: 1px solid rgba(212, 113, 110, 0.3);
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 20px;
  font-size: 0.85rem;
  color: var(--color-danger);
}

.error-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.error-icon svg {
  width: 100%;
  height: 100%;
}

.shake-enter-active {
  animation: shakeIn 0.4s ease;
}

.shake-leave-active {
  animation: shakeOut 0.3s ease;
}

@keyframes shakeIn {
  0% { opacity: 0; transform: translateX(-10px); }
  50% { transform: translateX(5px); }
  100% { opacity: 1; transform: translateX(0); }
}

@keyframes shakeOut {
  0% { opacity: 1; }
  100% { opacity: 0; transform: translateY(-5px); }
}

/* ================================
   Form
   ================================ */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: var(--color-text-subtle);
  z-index: 1;
  transition: color 0.3s ease;
}

.input-icon svg {
  width: 100%;
  height: 100%;
}

.input-field {
  width: 100%;
  padding: 14px 0 14px 36px;
  background: transparent;
  border: none;
  border-bottom: 1px solid var(--color-text-subtle);
  font-family: var(--font-body);
  font-size: 1rem;
  color: var(--color-text);
  outline: none;
  transition: border-color 0.3s ease;
}

.input-field::placeholder {
  color: var(--color-text-subtle);
}

.input-field:focus {
  border-bottom-color: var(--color-primary);
}

.input-field:focus ~ .input-icon {
  color: var(--color-primary);
}

.input-group.has-error .input-field {
  border-bottom-color: var(--color-danger);
}

.input-field:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    var(--color-primary),
    var(--color-accent)
  );
  transition: all 0.4s ease;
  transform: translateX(-50%);
}

.input-field:focus ~ .input-underline {
  width: 100%;
}

/* Field error */
.field-error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: -10px;
  font-size: 0.8rem;
  color: var(--color-danger);
}

.field-error svg {
  width: 14px;
  height: 14px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* ================================
   Submit Button
   ================================ */
.btn-submit {
  position: relative;
  width: 100%;
  padding: 16px;
  margin-top: 8px;
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-primary-dark) 100%
  );
  border: none;
  border-radius: 14px;
  color: #1a1f1c;
  font-family: var(--font-body);
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 20px rgba(201, 168, 108, 0.25);
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
    rgba(255, 255, 255, 0.2) 0%,
    transparent 50%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(201, 168, 108, 0.35);
}

.btn-submit:hover:not(:disabled) .btn-glow {
  opacity: 1;
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* ================================
   Spinner
   ================================ */
.spinner {
  width: 22px;
  height: 22px;
  border: 2px solid rgba(26, 31, 28, 0.3);
  border-top-color: #1a1f1c;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ================================
   Switch Text
   ================================ */
.switch-text {
  text-align: center;
  margin-top: 28px;
  font-family: var(--font-body);
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.switch-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: all 0.2s ease;
  position: relative;
}

.switch-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--color-primary);
  transition: width 0.3s ease;
}

.switch-link:hover::after {
  width: 100%;
}

/* ================================
   Responsive
   ================================ */
@media (max-width: 480px) {
  .register-card {
    padding: 36px 28px 32px;
    border-radius: 20px;
  }

  .brand-title {
    font-size: 1.85rem;
  }

  .brand-subtitle {
    font-size: 0.75rem;
  }

  .botanical-1,
  .botanical-2,
  .botanical-3 {
    opacity: 0.3;
  }
}
</style>
