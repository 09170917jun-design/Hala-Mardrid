import { useState, type FormEvent } from 'react'

export default function Login() {
  const [mode, setMode] = useState<'login' | 'signup'>('login')

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
  }

  return (
    <div className="container page auth-wrap">
      <div className="card auth-card">
        <h1 className="page-title">{mode === 'login' ? '로그인' : '회원가입'}</h1>
        <form onSubmit={onSubmit} className="form">
          <label>
            이메일
            <input type="email" placeholder="you@example.com" required />
          </label>
          {mode === 'signup' && (
            <label>
              닉네임
              <input type="text" placeholder="마드리디스타" required />
            </label>
          )}
          <label>
            비밀번호
            <input type="password" placeholder="8자 이상" required />
          </label>
          <button type="submit" className="btn btn-primary btn-block">
            {mode === 'login' ? '로그인' : '가입하기'}
          </button>
        </form>
        <div className="divider">또는</div>
        <button type="button" className="btn btn-kakao btn-block">
          카카오로 계속하기
        </button>
        <p className="muted auth-switch">
          {mode === 'login' ? '아직 계정이 없으신가요?' : '이미 계정이 있으신가요?'}{' '}
          <button type="button" className="link-btn" onClick={() => setMode(mode === 'login' ? 'signup' : 'login')}>
            {mode === 'login' ? '회원가입' : '로그인'}
          </button>
        </p>
        <p className="muted notice">※ 디자인 확인용 화면입니다. 실제 인증은 2단계에서 연결됩니다.</p>
      </div>
    </div>
  )
}
