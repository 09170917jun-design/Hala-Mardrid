import type { Position } from '../api/football'

// 알려진 팀은 한국어로, 모르는 팀은 API가 준 이름을 그대로 보여 준다.
const TEAM_NAMES: Record<string, string> = {
  'Real Madrid': '레알 마드리드',
  Atleti: '아틀레티코',
  'Barça': '바르셀로나',
  'Sevilla FC': '세비야',
  Villarreal: '비야레알',
  Valencia: '발렌시아',
  Athletic: '아틀레틱 빌바오',
  'Real Sociedad': '레알 소시에다드',
  'Real Betis': '레알 베티스',
  Celta: '셀타 비고',
  Osasuna: '오사수나',
  'Rayo Vallecano': '라요 바예카노',
  Getafe: '헤타페',
  Espanyol: '에스파뇰',
  Alavés: '알라베스',
  Elche: '엘체',
  Levante: '레반테',
  Deportivo: '데포르티보',
  Málaga: '말라가',
  Santander: '라싱 산탄데르',
  Arsenal: '아스널',
  Inter: '인터 밀란',
  Roma: 'AS 로마',
  'RB Leipzig': 'RB 라이프치히',
  PSV: 'PSV',
  LASK: 'LASK',
  Shaktar: '샤흐타르',
  'PAE AEK': 'AEK 아테네',
}

const COMPETITION_NAMES: Record<string, string> = {
  PD: '라리가',
  CL: '챔피언스리그',
}

export const teamName = (name: string) => TEAM_NAMES[name] ?? name
export const competitionName = (code: string, fallback: string) => COMPETITION_NAMES[code] ?? fallback
export const isRealMadrid = (name: string) => name === 'Real Madrid'

export const positionLabel: Record<Position, string> = {
  GK: '골키퍼',
  DF: '수비수',
  MF: '미드필더',
  FW: '공격수',
}

/** 만 나이 */
export const ageOf = (dateOfBirth: string, now = new Date()) => {
  const birth = new Date(dateOfBirth)
  let age = now.getFullYear() - birth.getFullYear()
  const beforeBirthday =
    now.getMonth() < birth.getMonth() || (now.getMonth() === birth.getMonth() && now.getDate() < birth.getDate())
  if (beforeBirthday) age -= 1
  return age
}
