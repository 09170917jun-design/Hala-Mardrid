import { useState } from 'react'
import { players, positionLabel, type Player } from '../data/mock'

const filters: Array<{ key: 'ALL' | Player['position']; label: string }> = [
  { key: 'ALL', label: '전체' },
  { key: 'GK', label: positionLabel.GK },
  { key: 'DF', label: positionLabel.DF },
  { key: 'MF', label: positionLabel.MF },
  { key: 'FW', label: positionLabel.FW },
]

export default function Players() {
  const [filter, setFilter] = useState<(typeof filters)[number]['key']>('ALL')
  const list = players.filter((p) => filter === 'ALL' || p.position === filter)

  return (
    <div className="container page">
      <h1 className="page-title">선수단</h1>
      <p className="muted notice">※ 현재 화면의 선수 정보는 디자인 확인용 샘플입니다.</p>
      <div className="tabs">
        {filters.map((f) => (
          <button key={f.key} className={filter === f.key ? 'active' : ''} onClick={() => setFilter(f.key)}>
            {f.label}
          </button>
        ))}
      </div>
      <div className="grid grid-4">
        {list.map((p) => (
          <article key={p.id} className="card player-card">
            <div className="player-number">{p.number}</div>
            <h3>{p.name}</h3>
            <p className="muted">
              {positionLabel[p.position]} · {p.nationality}
            </p>
            {p.rating && (
              <p className="player-rating">
                평균 평점 <strong>{p.rating.toFixed(1)}</strong>
              </p>
            )}
          </article>
        ))}
      </div>
    </div>
  )
}
