import client from './client'
import type { ApiResponse, PageResult } from './client'

export interface EventResponse {
  id: number
  coupleId: number
  authorId: number
  authorNickname: string
  title: string
  content: string
  eventType: string
  eventDate: string
  images: string[]
  mood: string
  createdAt: string
}

export interface CreateEventRequest {
  title: string
  content?: string
  eventType: string
  eventDate: string
  images?: string[]
  mood?: string
}

export interface UpdateEventRequest {
  title?: string
  description?: string
  eventDate?: string
  eventType?: string
}

export interface GetEventsParams {
  page: number
  size: number
  eventType?: string
}

export async function getEvents(params: GetEventsParams): Promise<PageResult<EventResponse>> {
  const response = await client.get<ApiResponse<PageResult<EventResponse>>>('/events', { params })
  return response.data.data
}

export async function createEvent(data: CreateEventRequest): Promise<EventResponse> {
  const response = await client.post<ApiResponse<EventResponse>>('/events', data)
  return response.data.data
}

export async function getEvent(id: number): Promise<EventResponse> {
  const response = await client.get<ApiResponse<EventResponse>>(`/events/${id}`)
  return response.data.data
}

export async function updateEvent(id: number, data: UpdateEventRequest): Promise<EventResponse> {
  const response = await client.put<ApiResponse<EventResponse>>(`/events/${id}`, data)
  return response.data.data
}

export async function deleteEvent(id: number): Promise<void> {
  await client.delete<ApiResponse<null>>(`/events/${id}`)
}
