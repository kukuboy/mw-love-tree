import client from './client'
import type { ApiResponse } from './client'

export interface MessageResponse {
  id: number
  coupleId: number
  senderId: number
  senderNickname: string
  content: string
  isRead: boolean
  createdAt: string
}

export async function getMessages(): Promise<MessageResponse[]> {
  const response = await client.get<ApiResponse<MessageResponse[]>>('/messages')
  return response.data.data
}

export async function sendMessage(content: string): Promise<MessageResponse> {
  const response = await client.post<ApiResponse<MessageResponse>>('/messages', { content })
  return response.data.data
}

export async function getUnreadCount(): Promise<number> {
  const response = await client.get<ApiResponse<number>>('/messages/unread-count')
  return response.data.data
}

export async function markRead(id: number): Promise<void> {
  await client.put<ApiResponse<null>>(`/messages/${id}/read`)
}
