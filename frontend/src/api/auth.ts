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
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const response = await client.post<ApiResponse<AuthResponse>>('/auth/login', data)
  return response.data.data
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const response = await client.post<ApiResponse<AuthResponse>>('/auth/register', data)
  return response.data.data
}
