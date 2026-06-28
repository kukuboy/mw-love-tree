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
        const info = await fetchCoupleInfoApi()
        this.partnerInfo = info

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
        this.inviteCode = res.inviteCode ?? null
      } finally {
        this.loading = false
      }
    },

    async joinCouple(code: string) {
      const newToken = await joinCoupleApi(code)

      const authStore = useAuthStore()
      authStore.token = newToken
      localStorage.setItem('token', newToken)

      await this.fetchCoupleInfo()
    },
  },
})
