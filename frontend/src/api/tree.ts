import client from './client'
import type { ApiResponse } from './client'

export interface TreeResponse {
  id: number
  coupleId: number
  growthValue: number
  level: number
  milestones: Array<{
    id: number
    title: string
    description: string
    achievedAt: string
  }>
}

export interface TreeStatsResponse {
  totalEvents: number
  totalPhotos: number
  totalMessages: number
  daysSinceStart: number
}

export async function getTree(): Promise<TreeResponse> {
  const response = await client.get<ApiResponse<TreeResponse>>('/tree')
  return response.data.data
}

export async function getTreeStats(): Promise<TreeStatsResponse> {
  const response = await client.get<ApiResponse<TreeStatsResponse>>('/tree/stats')
  return response.data.data
}
