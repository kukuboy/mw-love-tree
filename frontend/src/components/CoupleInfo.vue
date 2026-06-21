<template>
  <div class="couple-info card">
    <h3 class="section-title">伴侣信息</h3>

    <div v-if="partnerInfo" class="info-content">
      <div class="partner-row">
        <div class="partner-avatar">
          <img
            v-if="partnerInfo.partnerAvatar"
            :src="partnerInfo.partnerAvatar"
            alt="伴侣头像"
            class="avatar-img"
          />
          <div v-else class="avatar-placeholder">💑</div>
        </div>
        <div class="partner-details">
          <span class="partner-nickname">{{ partnerInfo.partnerNickname }}</span>
        </div>
      </div>

      <div class="info-row">
        <span class="info-label">邀请码</span>
        <div class="invite-code-row">
          <code class="invite-code">{{ partnerInfo.inviteCode }}</code>
          <button
            class="copy-btn"
            @click="copyInviteCode"
            :title="copied ? '已复制' : '复制'"
          >
            {{ copied ? '✓' : '复制' }}
          </button>
        </div>
      </div>

      <div class="info-row">
        <span class="info-label">纪念日</span>
        <span class="info-value">{{ formattedAnniversary }}</span>
      </div>

      <div class="info-row">
        <span class="info-label">在一起</span>
        <span class="info-value days-value">{{ partnerInfo.daysTogether }} 天</span>
      </div>
    </div>

    <div v-else class="info-empty">暂无伴侣信息</div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useCoupleStore } from '@/stores/couple'

const coupleStore = useCoupleStore()
const partnerInfo = computed(() => coupleStore.partnerInfo)

const formattedAnniversary = computed(() => {
  if (!partnerInfo.value?.anniversary) return '-'
  const d = new Date(partnerInfo.value.anniversary)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const copied = ref(false)

async function copyInviteCode() {
  if (!partnerInfo.value?.inviteCode) return
  try {
    await navigator.clipboard.writeText(partnerInfo.value.inviteCode)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (e) {
    console.error('Failed to copy:', e)
  }
}
</script>

<style scoped>
.couple-info {
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.partner-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.partner-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(232, 160, 191, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.partner-nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-label {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.55;
}

.info-value {
  font-size: 14px;
  color: var(--color-text);
  font-weight: 500;
}

.days-value {
  color: var(--color-accent);
  font-weight: 700;
}

.invite-code-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.invite-code {
  padding: 4px 10px;
  border-radius: 6px;
  background: rgba(232, 160, 191, 0.08);
  font-size: 13px;
  font-family: monospace;
  color: var(--color-text);
  letter-spacing: 0.5px;
}

.copy-btn {
  padding: 4px 12px;
  border: 1px solid var(--color-primary);
  border-radius: 6px;
  background: transparent;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: var(--font-family);
}

.copy-btn:hover {
  background: var(--color-primary);
  color: white;
}

.info-empty {
  padding: 20px;
  text-align: center;
  font-size: 14px;
  color: var(--color-text);
  opacity: 0.5;
}
</style>
