import { useEffect, useState } from 'react'

type Health = { status: string; db?: string }

export default function Footer() {
  const [state, setState] = useState<'loading' | 'ok' | 'db-down' | 'down'>('loading')

  useEffect(() => {
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json() as Promise<Health>
      })
      .then((h) => setState(h.db === 'up' ? 'ok' : 'db-down'))
      .catch(() => setState('down'))
  }, [])

  const label = {
    loading: '서버 확인 중...',
    ok: '서버·DB 정상',
    'db-down': '서버 정상 · DB 연결 안 됨',
    down: '서버 연결 안 됨',
  }[state]

  return (
    <footer className="footer">
      <div className="container">
        <p className="footer-brand">Hala Madrid</p>
        <p>
          본 사이트는 팬이 만든 <strong>비공식 팬 사이트</strong>이며, 레알 마드리드 C.F.와 공식적인 관련이 없습니다.
        </p>
        <p className="footer-sub">© 2026 Hala Madrid Fan Project</p>
        <p className={`server-status ${state}`}>● {label}</p>
      </div>
    </footer>
  )
}
