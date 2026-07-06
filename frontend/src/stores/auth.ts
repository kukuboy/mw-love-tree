import { defineStore } from 'pinia'
import { login as authLogin, register as authRegister } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null as string | null,
    userId: null as number | null,
    nickname: null as string | null,
    coupleId: null as number | null,
    avatar: null as string | null,
    isLoggedIn: false,
  }),

  actions: {
    async login(email: string, password: string) {
      const data = await authLogin({ email, password })
      this.token = data.token ?? null
      this.userId = data.userId ?? null
      this.nickname = data.nickname ?? null
      this.coupleId = data.coupleId ?? null
      this.avatar = data.avatar ?? null
      this.isLoggedIn = true

      if (this.token) {
        localStorage.setItem('token', this.token)
        if (this.userId != null) localStorage.setItem('userId', String(this.userId))
        if (this.nickname) localStorage.setItem('nickname', this.nickname)
        if (this.coupleId != null) localStorage.setItem('coupleId', String(this.coupleId))
        if (this.avatar) localStorage.setItem('avatar', this.avatar)
      }
    },

    async register(email: string, password: string, nickname: string) {
      const data = await authRegister({ email, password, nickname })
      this.token = data.token ?? null
      this.userId = data.userId ?? null
      this.nickname = data.nickname ?? null
      this.coupleId = data.coupleId ?? null
      this.avatar = data.avatar ?? null
      this.isLoggedIn = true

      if (this.token) {
        localStorage.setItem('token', this.token)
        if (this.userId != null) localStorage.setItem('userId', String(this.userId))
        if (this.nickname) localStorage.setItem('nickname', this.nickname)
        if (this.coupleId != null) localStorage.setItem('coupleId', String(this.coupleId))
        if (this.avatar) localStorage.setItem('avatar', this.avatar)
      }
    },

    setAvatar(avatar: string) {
      this.avatar = avatar
      if (avatar) {
        localStorage.setItem('avatar', avatar)
      } else {
        localStorage.removeItem('avatar')
      }
    },

    logout() {
      this.token = null
      this.userId = null
      this.nickname = null
      this.coupleId = null
      this.avatar = null
      this.isLoggedIn = false

      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('nickname')
      localStorage.removeItem('coupleId')
      localStorage.removeItem('avatar')

      router.push('/login')
    },

    checkAuth() {
      const token = localStorage.getItem('token')
      if (token) {
        this.token = token
        this.userId = Number(localStorage.getItem('userId')) || null
        this.nickname = localStorage.getItem('nickname') || null
        this.coupleId = Number(localStorage.getItem('coupleId')) || null
        this.avatar = localStorage.getItem('avatar') || null
        this.isLoggedIn = true
      } else {
        this.isLoggedIn = false
      }
    },
  },
})
