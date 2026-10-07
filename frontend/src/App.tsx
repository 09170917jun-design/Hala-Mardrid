import { useEffect, useState } from 'react'
import './App.css'

type Health = { status: string; service: string }

function App() {
  const [health, setHealth] = useState<Health | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json() as Promise<Health>
      })
      .then(setHealth)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <main className="hero">
      <h1>Hala Madrid</h1>
      <p className="tagline">레알 마드리드 비공식 팬 서비스</p>
      <p className="status">
        백엔드 상태:{' '}
        {health ? `${health.status} (${health.service})` : error ? `연결 실패 - ${error}` : '확인 중... (첫 요청은 최대 1분 걸릴 수 있어요)'}
      </p>
    </main>
  )
}

export default App
