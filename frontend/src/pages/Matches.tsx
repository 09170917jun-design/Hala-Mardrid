import { useCallback, useState } from 'react'
import { listMatches, type MatchView } from '../api/football'
import { useFetch } from '../api/useFetch'
import MatchCard from '../components/MatchCard'

const tabs: Array<{ key: MatchView; label: string }> = [
  { key: 'upcoming', label: '예정 경기' },
  { key: 'finished', label: '지난 경기' },
]

export default function Matches() {
  const [view, setView] = useState<MatchView>('upcoming')
  const load = useCallback(() => listMatches(view), [view])
  const { data, error, loading } = useFetch(load)

  return (
    <div className="container page">
      <h1 className="page-title">경기 일정 · 결과</h1>
      <div className="tabs" role="tablist">
        {tabs.map((t) => (
          <button
            key={t.key}
            role="tab"
            aria-selected={view === t.key}
            className={view === t.key ? 'active' : ''}
            onClick={() => setView(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading && <p className="muted">불러오는 중... (서버가 잠들어 있었다면 1분쯤 걸릴 수 있어요)</p>}
      {error && <p className="form-error">{error}</p>}
      {data && data.length === 0 && (
        <div className="card empty">
          <p>표시할 경기가 아직 없어요. 데이터를 준비 중일 수 있으니 잠시 후 다시 확인해 주세요.</p>
        </div>
      )}
      {data && data.length > 0 && (
        <div className="grid grid-3">
          {data.map((m) => (
            <MatchCard key={m.id} match={m} />
          ))}
        </div>
      )}
      <p className="muted notice data-source">경기 정보 제공: football-data.org (점수는 실제보다 늦게 반영될 수 있습니다)</p>
    </div>
  )
}
