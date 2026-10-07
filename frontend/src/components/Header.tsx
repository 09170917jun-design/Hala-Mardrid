import { Link, NavLink } from 'react-router-dom'

const links = [
  { to: '/', label: '홈', end: true },
  { to: '/matches', label: '경기' },
  { to: '/players', label: '선수단' },
  { to: '/board', label: '게시판' },
]

export default function Header() {
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
          <Link to="/login" className="btn btn-primary nav-login">
            로그인
          </Link>
        </nav>
      </div>
    </header>
  )
}
