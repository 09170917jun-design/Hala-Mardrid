import { useState } from 'react'
import { Link } from 'react-router-dom'
import { listPlayers, type Position } from '../api/football'
import { useFetch } from '../api/useFetch'
import { positionLabel } from '../utils/football'

const filters: Array<{ key: 'ALL' | Position; label: string }> = [
  { key: 'ALL', label: '전체' },
  { key: 'GK', label: positionLabel.GK },
  { key: 'DF', label: positionLabel.DF },
  { key: 'MF', label: positionLabel.MF },
  { key: 'FW', label: positionLabel.FW },
]

export default function Players() {
  const [filter, setFilter] = useState<(typeof filters)[number]['key']>('ALL')
  const { data, error, loading } = useFetch(listPlayers)
  const list = data?.filter((p) => filter === 'ALL' || p.position === filter)

  return (
    <div className="container page">
      <h1 className="page-title">선수단</h1>
      <div className="tabs">
        {filters.map((f) => (
          <button key={f.key} className={filter === f.key ? 'active' : ''} onClick={() => setFilter(f.key)}>
            {f.label}
          </button>
        ))}
      </div>

      {loading && <p className="muted">불러오는 중... (서버가 잠들어 있었다면 1분쯤 걸릴 수 있어요)</p>}
      {error && <p className="form-error">{error}</p>}
      {list && list.length === 0 && (
        <div className="card empty">
          <p>표시할 선수가 아직 없어요. 데이터를 준비 중일 수 있으니 잠시 후 다시 확인해 주세요.</p>
        </div>
      )}
      {list && list.length > 0 && (
        <div className="grid grid-4">
          {list.map((p) => (
            <Link key={p.id} to={`/players/${p.id}`} className="card player-card">
              <div className="player-number">{p.shirtNumber ?? p.position}</div>
              <h3>{p.name}</h3>
              <p className="muted">
                {positionLabel[p.position]}
                {p.nationality ? ` · ${p.nationality}` : ''}
              </p>
            </Link>
          ))}
        </div>
      )}
      <p className="muted notice data-source">선수단 정보 제공: football-data.org</p>
    </div>
  )
}
