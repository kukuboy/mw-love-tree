// ============================================================
// TypeScript interfaces matching backend DTOs
// ============================================================

// ---- Auth ----

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  nickname?: string
}

export interface AuthResponse {
  token: string
  userId: number
  nickname: string
  coupleId: number | null
}

// ---- Couple ----

export interface InviteResponse {
  inviteCode: string
}

export interface CoupleInfoResponse {
  partnerNickname: string
  partnerAvatar: string
  inviteCode: string
  anniversary: string
  togetherDate: string
  daysTogether: number
}

// ---- Tree ----

export interface TreeResponse {
  stage: string
  leafCount: number
  flowerCount: number
  stageLabel: string
}

export interface TreeStatsResponse {
  totalEvents: number
  typeDistribution: Record<string, number>
  monthlyCounts: Record<string, number>
}

// ---- Event ----

export interface CreateEventRequest {
  title: string
  content?: string
  eventType: string
  eventDate: string
  images?: string[]
  mood?: string
}

export interface EventResponse {
  id: number
  coupleId: number
  authorId: number
  title: string
  content: string
  eventType: string
  eventDate: string
  images: string[]
  mood: string
  createdAt: string
  authorNickname: string
}

// ---- Message ----

export interface MessageResponse {
  id: number
  fromId: number
  toId: number
  content: string
  isRead: boolean
  createdAt: string
  fromNickname: string
}

// ---- Photo ----

export interface PhotoResponse {
  id: number
  url: string
  caption: string
  uploaderId: number
  uploaderNickname: string
  createdAt: string
}

// ---- Special Day ----

export interface SpecialDayResponse {
  name: string
  date: string
  daysUntil: number
  daysTogether: number
}

// ---- Common (paginated result) ----

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}
