import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'

const client: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Generic API response wrapper from backend
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

// Paginated result wrapper used by list endpoints
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

client.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    return response
  },
  (error: AxiosError<ApiResponse>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  },
)

export default client
