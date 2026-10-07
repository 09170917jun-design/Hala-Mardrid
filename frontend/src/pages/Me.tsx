import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Me() {
  const { user, ready, logout } = useAuth()
  const navigate = useNavigate()

  if (!ready) return <div className="container page muted">불러오는 중...</div>
  if (!user) return <Navigate to="/login" state={{ from: '/me' }} replace />

  const onLogout = async () => {
    await logout()
    navigate('/', { replace: true })
  }

  return (
    <div className="container page">
      <h1 className="page-title">내 정보</h1>
      <div className="card profile">
        <dl>
          <div>
            <dt>닉네임</dt>
            <dd>{user.nickname}</dd>
          </div>
          <div>
            <dt>이메일</dt>
            <dd>{user.email ?? '카카오 계정'}</dd>
          </div>
          <div>
            <dt>등급</dt>
            <dd>{user.role === 'ADMIN' ? '관리자' : '일반 회원'}</dd>
          </div>
        </dl>
        <button type="button" className="btn btn-ghost" onClick={onLogout}>
          로그아웃
        </button>
      </div>
    </div>
  )
}
