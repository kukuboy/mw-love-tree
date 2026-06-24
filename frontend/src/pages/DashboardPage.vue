<template>
  <div class="dashboard-page animate-fade-in-up">
    <!-- Top row: tree canvas (left) + side panel (right) -->
    <div class="dashboard-top">
      <div class="tree-section">
        <div class="tree-canvas-container">
          <LoveTreeCanvas
            v-if="treeStore.tree"
            :stage="treeStore.tree.stage"
            :leaf-count="treeStore.tree.leafCount"
            :flower-count="treeStore.tree.flowerCount"
          />
          <div v-else class="tree-loading">
            <div class="loading-spinner" />
          </div>
        </div>

        <div v-if="treeStore.tree" class="stage-badge-wrapper">
          <TreeStageBadge
            :stage="treeStore.tree.stage"
            :stage-label="treeStore.tree.stageLabel"
          />
        </div>
      </div>

      <div class="side-panel">
        <!-- Next special day countdown -->
        <CountdownCard
          v-if="nextSpecialDay"
          :name="nextSpecialDay.name"
          :date="nextSpecialDay.date"
          :days-until="nextSpecialDay.daysUntil"
        />

        <!-- Tree stats card -->
        <div v-if="treeStats" class="stats-card card">
          <div class="stat-item">
            <span class="stat-icon">📝</span>
            <span class="stat-label">总事件</span>
            <span class="stat-value">{{ treeStats.totalEvents }}</span>
          </div>
          <div v-if="coupleInfo" class="stat-item">
            <span class="stat-icon">💑</span>
            <span class="stat-label">在一起</span>
            <span class="stat-value">{{ coupleInfo.daysTogether }} 天</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom: recent events -->
    <div class="dashboard-bottom">
      <RecentEvents />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTreeStore } from '@/stores/tree'
import { useCoupleStore } from '@/stores/couple'
import { getSpecialDays } from '@/api/specialDays'
import type { SpecialDayResponse as ApiSpecialDay } from '@/api/specialDays'
import LoveTreeCanvas from '@/components/LoveTreeCanvas.vue'
import TreeStageBadge from '@/components/TreeStageBadge.vue'
import CountdownCard from '@/components/CountdownCard.vue'
import RecentEvents from '@/components/RecentEvents.vue'

const treeStore = useTreeStore()
const coupleStore = useCoupleStore()

// ---- Data ----
const specialDays = ref<ApiSpecialDay[]>([])
const treeStats = computed(() => treeStore.stats)
const coupleInfo = computed(() => coupleStore.partnerInfo)

// ---- Computed: next upcoming special day ----
interface UpcomingDay {
  name: string
  date: string
  daysUntil: number
}

/** Calculate days from today until the next occurrence of a date (month-day). */
function calcDaysUntil(dateStr: string): number {
  const now = new Date()
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return Infinity

  // Set to this year
  d.setFullYear(now.getFullYear())

  // If this year's date has already passed, look at next year
  if (d < now) {
    d.setFullYear(now.getFullYear() + 1)
  }

  return Math.ceil((d.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
}

const nextSpecialDay = computed<UpcomingDay | null>(() => {
  if (specialDays.value.length === 0) return null

  const withDays = specialDays.value.map((d) => ({
    name: d.name,
    date: d.date,
    daysUntil: calcDaysUntil(d.date),
  }))

  // Filter out dates still in the past (calc error guard) and pick the closest
  const upcoming = withDays
    .filter((d) => d.daysUntil >= 0)
    .sort((a, b) => a.daysUntil - b.daysUntil)

  if (upcoming.length === 0) return null

  // If the closest is actually 0 (today), return it regardless
  // "daysUntil === 0" means the date IS today (after yearly roll-over)
  return upcoming[0]
})

// ---- Lifecycle ----
onMounted(async () => {
  // Parallel fetches — tree, stats, couple info
  await Promise.allSettled([
    treeStore.fetchTree(),
    treeStore.fetchStats(),
    coupleStore.fetchCoupleInfo(),
  ])

  // Special days (separate try/catch in case it fails)
  try {
    const days = await getSpecialDays()
    specialDays.value = days
  } catch (e) {
    console.error('Failed to fetch special days:', e)
  }
})
</script>

<style scoped>
.dashboard-page {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
}

/* ---- Top row ---- */
.dashboard-top {
  display: flex;
  gap: 24px;
  margin-bottom: 28px;
}

/* Tree section (takes remaining space) */
.tree-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.tree-canvas-container {
  width: 100%;
}

/* Loading state */
.tree-loading {
  width: 100%;
  min-height: 400px;
  max-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.3);
  border-radius: var(--border-radius);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-secondary);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.stage-badge-wrapper {
  display: flex;
  justify-content: center;
}

/* ---- Side panel ---- */
.side-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Stats card */
.stats-card {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-icon {
  font-size: 18px;
  width: 26px;
  text-align: center;
  flex-shrink: 0;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.55;
  flex: 1;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-accent);
  white-space: nowrap;
}

/* ---- Bottom section ---- */
.dashboard-bottom {
  width: 100%;
}

/* =============================================================
   Responsive
   ============================================================= */

/* Tablet and smaller: side panel goes below */
@media (max-width: 900px) {
  .dashboard-top {
    flex-direction: column;
    gap: 20px;
  }

  .side-panel {
    width: 100%;
    flex-direction: row;
    gap: 12px;
  }

  .side-panel > * {
    flex: 1;
  }
}

/* Mobile */
@media (max-width: 768px) {
  .dashboard-page {
    padding: 16px;
  }

  .dashboard-top {
    gap: 16px;
  }

  .side-panel {
    flex-direction: column;
  }

  .tree-loading {
    min-height: 280px;
  }
}

@media (max-width: 480px) {
  .dashboard-page {
    padding: 12px;
  }

  .side-panel {
    flex-direction: column;
  }

  .stat-value {
    font-size: 15px;
  }
}
</style>
