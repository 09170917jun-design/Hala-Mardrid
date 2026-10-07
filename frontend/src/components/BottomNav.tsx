import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const icon = (path: ReactNode) => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    {path}
  </svg>
)

const items = [
  { to: '/', label: '홈', end: true, icon: icon(<path d="M3 11l9-8 9 8v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z" />) },
  {
    to: '/matches',
    label: '경기',
    icon: icon(
      <>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M3 10h18M8 3v4M16 3v4" />
      </>,
    ),
  },
  {
    to: '/players',
    label: '선수단',
    icon: icon(
      <>
        <circle cx="9" cy="8" r="3.5" />
        <path d="M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6M16 4.5a3.5 3.5 0 0 1 0 7M18 14.3c2 .8 3.5 2.6 3.5 5.7" />
      </>,
    ),
  },
  {
    to: '/board',
    label: '게시판',
    icon: icon(<path d="M4 5h16a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H9l-5 4V6a1 1 0 0 1 1-1z" />),
  },
  {
    to: '/login',
    label: '로그인',
    icon: icon(
      <>
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" />
      </>,
    ),
  },
]

export default function BottomNav() {
  const { user } = useAuth()
  // 마지막 탭은 로그인 상태에 따라 "로그인" / "내 정보"로 바뀐다.
  const tabs = items.map((i) => (i.to === '/login' && user ? { ...i, to: '/me', label: '내 정보' } : i))

  return (
    <nav className="bottom-nav" aria-label="하단 메뉴">
      {tabs.map((i) => (
        <NavLink key={i.to} to={i.to} end={i.end}>
          <span className="bottom-icon">{i.icon}</span>
          <span>{i.label}</span>
        </NavLink>
      ))}
    </nav>
  )
}
