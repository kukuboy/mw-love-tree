<template>
  <div class="settings-page animate-fade-in-up">
    <h2 class="page-title">设置</h2>

    <section class="settings-section card">
      <h3 class="section-title">个人资料</h3>
      <div class="profile-row">
        <AvatarUploader v-model="avatarFile" />
        <div class="profile-fields">
          <div class="field-group">
            <label class="field-label">昵称</label>
            <input
              v-model="nickname"
              class="field-input"
              placeholder="你的昵称"
              maxlength="20"
            />
          </div>
        </div>
      </div>
      <button class="btn-save" @click="saveProfile" :disabled="savingProfile">
        {{ savingProfile ? '保存中...' : '保存' }}
      </button>
    </section>

    <section class="settings-section">
      <CoupleInfo />
    </section>

    <section class="settings-section card">
      <h3 class="section-title">在一起日期</h3>
      <p class="section-desc">设置你们真正在一起的日期，将自动生成纪念日事件。</p>
      <div class="field-group">
        <label class="field-label">在一起日期</label>
        <input
          v-model="togetherDate"
          type="date"
          class="field-input"
        />
      </div>
      <button class="btn-save" @click="saveTogetherDate" :disabled="savingTogetherDate">
        {{ savingTogetherDate ? '保存中...' : '保存在一起日期' }}
      </button>
    </section>

    <section class="settings-section card danger-zone">
      <h3 class="section-title danger-title">危险区域</h3>
      <p class="danger-desc">解除配对将清空所有数据，此操作不可撤销。</p>
      <button class="btn-danger" @click="showConfirmModal = true">解除配对</button>
    </section>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showConfirmModal"
          class="modal-overlay"
          @click.self="showConfirmModal = false"
        >
          <div class="modal-dialog">
            <h3 class="modal-title">确认解除配对</h3>
            <p class="modal-desc">
              此操作不可撤销。请输入 <strong>确认解除</strong> 以确认：
            </p>
            <input
              v-model="confirmText"
              class="confirm-input"
              placeholder="输入「确认解除」"
              @keydown.enter="handleUnpair"
            />
            <div class="modal-actions">
              <button class="btn-cancel" @click="closeConfirmModal">取消</button>
              <button
                class="btn-danger-confirm"
                :disabled="confirmText !== '确认解除'"
                @click="handleUnpair"
              >
                确认解除
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCoupleStore } from '@/stores/couple'
import { setTogetherDate as setTogetherDateApi } from '@/api/couple'
import AvatarUploader from '@/components/AvatarUploader.vue'
import CoupleInfo from '@/components/CoupleInfo.vue'

const authStore = useAuthStore()
const coupleStore = useCoupleStore()

const avatarFile = ref<File | null>(null)
const nickname = ref(authStore.nickname || '')
const savingProfile = ref(false)

const togetherDate = ref('')
const savingTogetherDate = ref(false)

const showConfirmModal = ref(false)
const confirmText = ref('')

onMounted(async () => {
  await coupleStore.fetchCoupleInfo()
  // Initialize togetherDate from couple info
  if (coupleStore.partnerInfo?.togetherDate) {
    const d = new Date(coupleStore.partnerInfo.togetherDate)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    togetherDate.value = `${y}-${m}-${day}`
  }
})

async function saveProfile() {
  savingProfile.value = true
  try {
    if (nickname.value) {
      authStore.nickname = nickname.value
      localStorage.setItem('nickname', nickname.value)
    }
  } catch (e) {
    console.error('Failed to save profile:', e)
  } finally {
    savingProfile.value = false
  }
}

async function saveTogetherDate() {
  if (!togetherDate.value) return
  savingTogetherDate.value = true
  try {
    await setTogetherDateApi(togetherDate.value)
    await coupleStore.fetchCoupleInfo()
  } catch (e) {
    console.error('Failed to save together date:', e)
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
.settings-page {
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 20px;
}

.settings-section {
  padding: 20px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
}

.section-desc {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.55;
  margin-bottom: 14px;
  line-height: 1.5;
}

.profile-row {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.profile-fields {
  flex: 1;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.55;
}

.field-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text);
  outline: none;
  transition: border-color 0.2s;
  font-family: var(--font-family);
}

.field-input:focus {
  border-color: var(--color-primary);
}

.btn-save {
  width: 100%;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  background: var(--color-primary);
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
  font-family: var(--font-family);
  -webkit-tap-highlight-color: transparent;
}

.btn-save:hover:not(:disabled) {
  opacity: 0.85;
}

.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Danger zone */
.danger-zone {
  border: 1px solid rgba(255, 77, 79, 0.2);
}

.danger-title {
  color: #ff4d4f;
}

.danger-desc {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
  margin-bottom: 12px;
}

.btn-danger {
  width: 100%;
  padding: 12px 20px;
  border: 1px solid #ff4d4f;
  border-radius: 10px;
  background: transparent;
  color: #ff4d4f;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: var(--font-family);
  -webkit-tap-highlight-color: transparent;
}

.btn-danger:hover {
  background: #ff4d4f;
  color: white;
}

/* Confirm modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-dialog {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 100%;
  max-width: 380px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.modal-desc {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
  margin-bottom: 14px;
  line-height: 1.5;
}

.confirm-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(255, 77, 79, 0.3);
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text);
  outline: none;
  transition: border-color 0.2s;
  margin-bottom: 16px;
  font-family: var(--font-family);
}

.confirm-input:focus {
  border-color: #ff4d4f;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-danger-confirm {
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
  font-family: var(--font-family);
  -webkit-tap-highlight-color: transparent;
}

.btn-cancel {
  background: rgba(0, 0, 0, 0.06);
  color: var(--color-text);
}

.btn-danger-confirm {
  background: #ff4d4f;
  color: white;
}

.btn-danger-confirm:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-cancel:hover:not(:disabled),
.btn-danger-confirm:hover:not(:disabled) {
  opacity: 0.85;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---- Mobile responsive ---- */
@media (max-width: 768px) {
  .settings-page {
    padding: 16px;
  }

  .page-title {
    font-size: 18px;
    margin-bottom: 16px;
  }

  .settings-section {
    padding: 16px;
    margin-bottom: 12px;
  }

  .section-title {
    font-size: 15px;
    margin-bottom: 12px;
  }

  .profile-row {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .profile-fields {
    width: 100%;
  }

  .modal-overlay {
    padding: 16px;
    align-items: flex-end;
  }

  .modal-dialog {
    max-width: 100%;
    border-radius: 16px 16px 0 0;
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .settings-page {
    padding: 12px;
  }

  .settings-section {
    padding: 14px;
  }
}
</style>
