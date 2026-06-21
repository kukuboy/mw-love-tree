import { defineStore } from 'pinia'
import {
  getCoupleInfo as fetchCoupleInfoApi,
  getInviteCode as createInviteCodeApi,
  joinCouple as joinCoupleApi,
} from '@/api/couple'
import type { CoupleInfoResponse } from '@/types'
import { useAuthStore } from '@/stores/auth'

export const useCoupleStore = defineStore('couple', {
  state: () => ({
    partnerInfo: null as CoupleInfoResponse | null,
    inviteCode: null as string | null,
    loading: false,
  }),

  actions: {
    async fetchCoupleInfo() {
      this.loading = true
      try {
        const res = await fetchCoupleInfoApi()
        // Cast through `any` because the API layer types may not match the actual backend DTO
        const info = (res as any) as CoupleInfoResponse
        this.partnerInfo = info

        // Persist the invite code from couple info if available
        if (info?.inviteCode) {
          this.inviteCode = info.inviteCode
        }
      } finally {
        this.loading = false
      }
    },

    async createInvite() {
      this.loading = true
      try {
        const res = await createInviteCodeApi()
        // Backend returns { inviteCode: string }
        this.inviteCode = (res as any).inviteCode ?? null
      } finally {
        this.loading = false
      }
    },

    async joinCouple(code: string) {
      const newToken = await joinCoupleApi(code)

      // Update the stored JWT with the fresh token that includes coupleId
      const authStore = useAuthStore()
      authStore.token = newToken
      localStorage.setItem('token', newToken)

      // After successfully joining, refresh couple info to get the latest data
      await this.fetchCoupleInfo()
    },
  },
})
