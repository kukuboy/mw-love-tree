<template>
  <div class="couple-info">
    <div class="card-header">
      <div class="header-icon accent">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
      </div>
      <h2 class="section-title">伴侣信息</h2>
    </div>

    <div v-if="partnerInfo" class="info-content">
      <div class="partner-card">
        <div class="partner-avatar">
          <img
            v-if="partnerInfo.partnerAvatar"
            :src="partnerInfo.partnerAvatar"
            alt="伴侣头像"
            class="avatar-img"
          />
          <div v-else class="avatar-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </div>
        </div>
        <div class="partner-details">
          <span class="partner-name">{{ partnerInfo.partnerNickname }}</span>
          <span class="partner-label">你的伴侣</span>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <path d="M16 2v4M8 2v4M3 10h18"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">在一起</span>
            <span class="stat-value">{{ partnerInfo.daysTogether }} 天</span>
          </div>
        </div>
      </div>

      <div class="info-divider"></div>

      <div class="info-row">
        <span class="info-label">
          <span class="label-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
          </span>
          邀请码
        </span>
        <div class="invite-code-wrapper">
          <code class="invite-code">{{ partnerInfo.inviteCode }}</code>
          <button
            class="copy-btn"
            @click="copyInviteCode"
            :title="copied ? '已复制' : '复制'"
          >
            <svg v-if="!copied" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="9" y="9" width="13" height="13" rx="2"/>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 6L9 17l-5-5"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="info-row">
        <span class="info-label">
          <span class="label-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 6v6l4 2"/>
            </svg>
          </span>
          绑定日期
        </span>
        <span class="info-value">{{ formattedAnniversary }}</span>
      </div>

      <div class="info-row" v-if="partnerInfo.togetherDate">
        <span class="info-label">
          <span class="label-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
          </span>
          纪念日
        </span>
        <span class="info-value accent">{{ formattedTogetherDate }}</span>
      </div>
    </div>

    <div v-else class="info-empty">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
      </div>
      <span class="empty-text">暂无伴侣信息</span>
      <span class="empty-hint">邀请你的伴侣开始记录吧</span>
    </div>
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

const formattedTogetherDate = computed(() => {
  if (!partnerInfo.value?.togetherDate) return '-'
  const d = new Date(partnerInfo.value.togetherDate)
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
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(201, 168, 108, 0.2);
}

.header-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(232, 180, 184, 0.15), rgba(232, 180, 184, 0.05));
  border: 1px solid rgba(232, 180, 184, 0.2);
  border-radius: 12px;
  color: var(--color-accent);
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

/* Partner Card */
.partner-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(232, 180, 184, 0.08), rgba(232, 180, 184, 0.03));
  border: 1px solid rgba(232, 180, 184, 0.15);
  border-radius: 16px;
  margin-bottom: 24px;
}

.partner-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.2), rgba(201, 168, 108, 0.1));
  border: 2px solid rgba(201, 168, 108, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-accent);
}

.avatar-placeholder svg {
  width: 28px;
  height: 28px;
}

.partner-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.partner-name {
  font-family: var(--font-display);
  font-size: 1.25rem;
  font-weight: 500;
  color: var(--color-text);
}

.partner-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  letter-spacing: 0.1em;
}

/* Stats */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(26, 31, 28, 0.4);
  border-radius: 12px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(201, 168, 108, 0.15), rgba(201, 168, 108, 0.05));
  border-radius: 10px;
  color: var(--color-primary);
}

.stat-icon svg {
  width: 18px;
  height: 18px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  letter-spacing: 0.05em;
}

.stat-value {
  font-family: var(--font-display);
  font-size: 1.35rem;
  font-weight: 500;
  color: var(--color-accent);
}

/* Info divider */
.info-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(201, 168, 108, 0.2), transparent);
  margin: 24px 0;
}

/* Info rows */
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.label-icon {
  width: 16px;
  height: 16px;
  color: var(--color-primary);
  opacity: 0.7;
}

.label-icon svg {
  width: 100%;
  height: 100%;
}

.info-value {
  font-family: var(--font-display);
  font-size: 0.95rem;
  color: var(--color-text);
}

.info-value.accent {
  color: var(--color-accent);
}

/* Invite code */
.invite-code-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.invite-code {
  padding: 6px 14px;
  background: rgba(201, 168, 108, 0.1);
  border: 1px dashed rgba(201, 168, 108, 0.3);
  border-radius: 8px;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.9rem;
  color: var(--color-primary);
  letter-spacing: 1px;
}

.copy-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(201, 168, 108, 0.1);
  border: 1px solid rgba(201, 168, 108, 0.2);
  border-radius: 8px;
  color: var(--color-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.copy-btn:hover {
  background: rgba(201, 168, 108, 0.2);
  transform: scale(1.05);
}

.copy-btn svg {
  width: 16px;
  height: 16px;
}

/* Empty state */
.info-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(232, 180, 184, 0.08);
  border: 1px dashed rgba(232, 180, 184, 0.2);
  border-radius: 50%;
  margin-bottom: 16px;
  color: var(--color-accent);
  opacity: 0.6;
}

.empty-icon svg {
  width: 32px;
  height: 32px;
}

.empty-text {
  font-family: var(--font-display);
  font-size: 1.1rem;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.empty-hint {
  font-size: 0.85rem;
  color: var(--color-text-subtle, #6a6560);
}

/* Mobile responsive */
@media (max-width: 480px) {
  .partner-card {
    padding: 16px;
  }

  .partner-avatar {
    width: 50px;
    height: 50px;
  }

  .partner-name {
    font-size: 1.1rem;
  }

  .stat-item {
    padding: 12px;
  }

  .stat-value {
    font-size: 1.15rem;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .invite-code-wrapper {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
