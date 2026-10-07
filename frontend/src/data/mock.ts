// 샘플 데이터 (디자인 확인용). 실제 데이터는 백엔드 연동 단계에서 교체한다.

export type Match = {
  id: number
  competition: string
  home: string
  away: string
  kickoff: string
  status: 'SCHEDULED' | 'FINISHED'
  homeScore?: number
  awayScore?: number
}

export type Player = {
  id: number
  name: string
  position: 'GK' | 'DF' | 'MF' | 'FW'
  number: number
  nationality: string
  rating?: number
}

export type Post = {
  id: number
  title: string
  author: string
  createdAt: string
  views: number
  likes: number
  comments: number
}

const day = 24 * 60 * 60 * 1000
const iso = (offsetDays: number, hour: number) => {
  const d = new Date(Date.now() + offsetDays * day)
  d.setHours(hour, 0, 0, 0)
  return d.toISOString()
}

export const matches: Match[] = [
  { id: 1, competition: '라리가', home: '레알 마드리드', away: '세비야', kickoff: iso(5, 21), status: 'SCHEDULED' },
  { id: 2, competition: '챔피언스리그', home: '바이에른 뮌헨', away: '레알 마드리드', kickoff: iso(12, 21), status: 'SCHEDULED' },
  { id: 3, competition: '라리가', home: '레알 마드리드', away: '발렌시아', kickoff: iso(19, 18), status: 'SCHEDULED' },
  { id: 4, competition: '라리가', home: '비야레알', away: '레알 마드리드', kickoff: iso(-3, 21), status: 'FINISHED', homeScore: 1, awayScore: 3 },
  { id: 5, competition: '챔피언스리그', home: '레알 마드리드', away: '인터 밀란', kickoff: iso(-10, 21), status: 'FINISHED', homeScore: 2, awayScore: 2 },
  { id: 6, competition: '라리가', home: '레알 마드리드', away: '아틀레티코', kickoff: iso(-17, 21), status: 'FINISHED', homeScore: 2, awayScore: 0 },
]

export const players: Player[] = [
  { id: 1, name: '선수 A', position: 'GK', number: 1, nationality: '벨기에', rating: 7.4 },
  { id: 2, name: '선수 B', position: 'DF', number: 2, nationality: '스페인', rating: 7.1 },
  { id: 3, name: '선수 C', position: 'DF', number: 3, nationality: '브라질', rating: 7.0 },
  { id: 4, name: '선수 D', position: 'DF', number: 4, nationality: '오스트리아', rating: 6.9 },
  { id: 5, name: '선수 E', position: 'MF', number: 8, nationality: '우루과이', rating: 7.6 },
  { id: 6, name: '선수 F', position: 'MF', number: 5, nationality: '잉글랜드', rating: 8.1 },
  { id: 7, name: '선수 G', position: 'MF', number: 15, nationality: '튀르키예', rating: 7.2 },
  { id: 8, name: '선수 H', position: 'FW', number: 7, nationality: '브라질', rating: 7.9 },
  { id: 9, name: '선수 I', position: 'FW', number: 9, nationality: '프랑스', rating: 8.3 },
  { id: 10, name: '선수 J', position: 'FW', number: 11, nationality: '브라질', rating: 7.5 },
]

export const posts: Post[] = [
  { id: 1, title: '어제 경기 후기 — 후반전 교체가 신의 한 수였다', author: '마드리디스타', createdAt: '2026-10-06', views: 482, likes: 37, comments: 21 },
  { id: 2, title: '이번 시즌 중원 조합 어떻게 보세요?', author: 'halamadrid', createdAt: '2026-10-05', views: 301, likes: 18, comments: 34 },
  { id: 3, title: '[공지] 게시판 이용 규칙 안내', author: '관리자', createdAt: '2026-10-01', views: 1204, likes: 52, comments: 3 },
  { id: 4, title: '베르나베우 직관 가는 팁 공유합니다', author: '직관러', createdAt: '2026-09-30', views: 655, likes: 61, comments: 19 },
  { id: 5, title: '챔스 조별리그 일정 정리', author: 'blanco', createdAt: '2026-09-28', views: 233, likes: 12, comments: 5 },
]

export const positionLabel: Record<Player['position'], string> = {
  GK: '골키퍼',
  DF: '수비수',
  MF: '미드필더',
  FW: '공격수',
}

export const formatKickoff = (value: string) =>
  new Date(value).toLocaleString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
