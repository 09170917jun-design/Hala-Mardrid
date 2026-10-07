import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const links = [
  { to: '/', label: '홈', end: true },
  { to: '/matches', label: '경기' },
  { to: '/players', label: '선수단' },
  { to: '/board', label: '게시판' },
]

export default function Header() {
  const { user, ready, logout } = useAuth()
  const navigate = useNavigate()

  const onLogout = async () => {
    // 먼저 홈으로 이동한 뒤 로그아웃한다. (로그인이 필요한 페이지가 로그인 화면으로 보내 버리는 것을 막는다)
    navigate('/', { replace: true })
    await logout()
  }

  return (
    <header className="header">
      <div className="container header-inner">
        <Link to="/" className="logo">
          <span className="logo-mark">HM</span>
          <span className="logo-text">Hala Madrid</span>
        </Link>

        <nav className="nav" aria-label="주 메뉴">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}>
              {l.label}
            </NavLink>
          ))}
          {ready &&
            (user ? (
              <>
                <NavLink to="/me" className="nav-user">
                  {user.nickname}
                </NavLink>
                <button type="button" className="btn btn-ghost nav-login" onClick={onLogout}>
                  로그아웃
                </button>
              </>
            ) : (
              <Link to="/login" className="btn btn-primary nav-login">
                로그인
              </Link>
            ))}
        </nav>
      </div>
    </header>
  )
}
