import client from './client'
import type { ApiResponse, PageResult } from './client'

export interface PhotoResponse {
  id: number
  coupleId: number
  url: string
  caption?: string
  uploadedAt: string
}

export async function getPhotos(page: number, size: number): Promise<PageResult<PhotoResponse>> {
  const response = await client.get<ApiResponse<PageResult<PhotoResponse>>>('/photos', {
    params: { page, size },
  })
  return response.data.data
}

export async function uploadPhoto(file: File, caption?: string): Promise<PhotoResponse> {
  const formData = new FormData()
  formData.append('file', file)
  if (caption !== undefined) {
    formData.append('caption', caption)
  }
  const response = await client.post<ApiResponse<PhotoResponse>>('/photos', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return response.data.data
}

export async function deletePhoto(id: number): Promise<void> {
  await client.delete<ApiResponse<null>>(`/photos/${id}`)
}
