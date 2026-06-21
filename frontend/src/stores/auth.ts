import { defineStore } from 'pinia'
import { login as authLogin, register as authRegister } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null as string | null,
    userId: null as number | null,
    nickname: null as string | null,
    coupleId: null as number | null,
    isLoggedIn: false,
  }),

  actions: {
    async login(email: string, password: string) {
      const res = await authLogin({ email, password })
      // The backend returns AuthResponse with { token, userId, nickname, coupleId }
      // Cast through `any` because the API layer types may not match the actual backend DTO
      const data = res as any
      this.token = data.token ?? null
      this.userId = data.userId ?? null
      this.nickname = data.nickname ?? null
      this.coupleId = data.coupleId ?? null
      this.isLoggedIn = true

      if (this.token) {
        localStorage.setItem('token', this.token)
        if (this.userId != null) localStorage.setItem('userId', String(this.userId))
        if (this.nickname) localStorage.setItem('nickname', this.nickname)
        if (this.coupleId != null) localStorage.setItem('coupleId', String(this.coupleId))
      }
    },

    async register(email: string, password: string, nickname: string) {
      const res = await authRegister({ email, password, nickname })
      const data = res as any
      this.token = data.token ?? null
      this.userId = data.userId ?? null
      this.nickname = data.nickname ?? null
      this.coupleId = data.coupleId ?? null
      this.isLoggedIn = true

      if (this.token) {
        localStorage.setItem('token', this.token)
        if (this.userId != null) localStorage.setItem('userId', String(this.userId))
        if (this.nickname) localStorage.setItem('nickname', this.nickname)
        if (this.coupleId != null) localStorage.setItem('coupleId', String(this.coupleId))
      }
    },

    logout() {
      this.token = null
      this.userId = null
      this.nickname = null
      this.coupleId = null
      this.isLoggedIn = false

      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('nickname')
      localStorage.removeItem('coupleId')

      router.push('/login')
    },

    checkAuth() {
      const token = localStorage.getItem('token')
      if (token) {
        this.token = token
        this.userId = Number(localStorage.getItem('userId')) || null
        this.nickname = localStorage.getItem('nickname') || null
        this.coupleId = Number(localStorage.getItem('coupleId')) || null
        this.isLoggedIn = true
      } else {
        this.isLoggedIn = false
      }
    },
  },
})
