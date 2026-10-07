import { api } from './client'

export type MatchStatus = 'SCHEDULED' | 'LIVE' | 'FINISHED' | 'POSTPONED' | 'CANCELLED'

export type MatchItem = {
  id: number
  competitionCode: string
  competitionName: string
  matchday: number | null
  homeTeam: string
  awayTeam: string
  kickoffAt: string
  status: MatchStatus
  homeScore: number | null
  awayScore: number | null
}

export type Position = 'GK' | 'DF' | 'MF' | 'FW'

export type PlayerItem = {
  id: number
  name: string
  position: Position
  shirtNumber: number | null
  nationality: string | null
  dateOfBirth: string | null
}

export type MatchView = 'upcoming' | 'finished'

export const listMatches = (view: MatchView, limit = 100) =>
  api<MatchItem[]>(`/api/matches?view=${view}&limit=${limit}`)

export const getMatch = (id: number) => api<MatchItem>(`/api/matches/${id}`)

export const listPlayers = () => api<PlayerItem[]>('/api/players')

export const getPlayer = (id: number) => api<PlayerItem>(`/api/players/${id}`)
