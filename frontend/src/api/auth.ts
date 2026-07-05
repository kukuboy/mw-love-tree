import client from './client'
import type { ApiResponse } from './client'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  nickname: string
}

export interface AuthResponse {
  token: string
  userId: number
  nickname: string
  coupleId: number | null
  avatar?: string | null
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const response = await client.post<ApiResponse<AuthResponse>>('/auth/login', data)
  return response.data.data
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const response = await client.post<ApiResponse<AuthResponse>>('/auth/register', data)
  return response.data.data
}

export async function uploadAvatar(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const response = await client.post<ApiResponse<{ avatar: string }>>('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return response.data.data.avatar
}
