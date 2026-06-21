import client from './client'
import type { ApiResponse } from './client'

export interface SpecialDayResponse {
  id: number
  coupleId: number
  name: string
  date: string
  type: string
  reminder: boolean
  createdAt: string
}

export async function getSpecialDays(): Promise<SpecialDayResponse[]> {
  const response = await client.get<ApiResponse<SpecialDayResponse[]>>('/special-days')
  return response.data.data
}
