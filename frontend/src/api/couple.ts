import client from './client'
import type { ApiResponse } from './client'

export interface InviteResponse {
  inviteCode: string
}

export interface CoupleInfoResponse {
  id: number
  partner1: {
    id: number
    nickname: string
    email: string
  }
  partner2: {
    id: number
    nickname: string
    email: string
  }
  createdAt: string
  inviteCode?: string
}

export async function getInviteCode(): Promise<InviteResponse> {
  const response = await client.post<ApiResponse<InviteResponse>>('/couple/invite')
  return response.data.data
}

export async function joinCouple(code: string): Promise<void> {
  const response = await client.post<ApiResponse<null>>('/couple/join', { inviteCode: code })
  // No data expected on success, but we don't throw — void return
  return
}

export async function getCoupleInfo(): Promise<CoupleInfoResponse> {
  const response = await client.get<ApiResponse<CoupleInfoResponse>>('/couple/info')
  return response.data.data
}
