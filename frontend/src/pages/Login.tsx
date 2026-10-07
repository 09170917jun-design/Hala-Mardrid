import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { startKakaoLogin } from '../auth/kakao'

export default function Login() {
  const { user, ready, login, signup } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (ready && user) return <Navigate to={from} replace />

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      if (mode === 'login') await login(email, password)
      else await signup(email, password, nickname)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.')
    } finally {
      setSubmitting(false)
    }
  }

  const onKakao = async () => {
    setError(null)
    setSubmitting(true)
    try {
      await startKakaoLogin(from)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.')
      setSubmitting(false)
    }
  }

  const switchMode = () => {
    setMode(mode === 'login' ? 'signup' : 'login')
    setError(null)
  }

  return (
    <div className="container page auth-wrap">
      <div className="card auth-card">
        <h1 className="page-title">{mode === 'login' ? '로그인' : '회원가입'}</h1>
        <form onSubmit={onSubmit} className="form">
          <label>
            이메일
            <input
              type="email"
              placeholder="you@example.com"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
          {mode === 'signup' && (
            <label>
              닉네임
              <input
                type="text"
                placeholder="마드리디스타"
                autoComplete="nickname"
                minLength={2}
                maxLength={12}
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                required
              />
            </label>
          )}
          <label>
            비밀번호
            <input
              type="password"
              placeholder="8자 이상"
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              minLength={mode === 'signup' ? 8 : undefined}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          {error && (
            <p className="form-error" role="alert">
              {error}
            </p>
          )}
          <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
            {submitting ? '처리 중...' : mode === 'login' ? '로그인' : '가입하기'}
          </button>
        </form>
        <div className="divider">또는</div>
        <button type="button" className="btn btn-kakao btn-block" disabled={submitting} onClick={onKakao}>
          카카오로 계속하기
        </button>
        <p className="muted auth-switch">
          {mode === 'login' ? '아직 계정이 없으신가요?' : '이미 계정이 있으신가요?'}{' '}
          <button type="button" className="link-btn" onClick={switchMode}>
            {mode === 'login' ? '회원가입' : '로그인'}
          </button>
        </p>
      </div>
    </div>
  )
}
