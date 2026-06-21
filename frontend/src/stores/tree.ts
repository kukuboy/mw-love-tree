import { defineStore } from 'pinia'
import { getTree as fetchTreeApi, getTreeStats as fetchTreeStatsApi } from '@/api/tree'
import type { TreeResponse, TreeStatsResponse } from '@/types'

export const useTreeStore = defineStore('tree', {
  state: () => ({
    tree: null as TreeResponse | null,
    stats: null as TreeStatsResponse | null,
    loading: false,
  }),

  actions: {
    async fetchTree() {
      this.loading = true
      try {
        const res = await fetchTreeApi()
        // Cast through `any` because the API layer types may not match the actual backend DTO
        this.tree = (res as any) as TreeResponse
      } finally {
        this.loading = false
      }
    },

    async fetchStats() {
      this.loading = true
      try {
        const res = await fetchTreeStatsApi()
        this.stats = (res as any) as TreeStatsResponse
      } finally {
        this.loading = false
      }
    },
  },
})
