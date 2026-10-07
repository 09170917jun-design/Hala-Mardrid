import { useState } from 'react'
import MatchCard from '../components/MatchCard'
import { matches } from '../data/mock'

const tabs = [
  { key: 'SCHEDULED', label: '예정 경기' },
  { key: 'FINISHED', label: '지난 경기' },
] as const

export default function Matches() {
  const [tab, setTab] = useState<(typeof tabs)[number]['key']>('SCHEDULED')
  const list = matches.filter((m) => m.status === tab)

  return (
    <div className="container page">
      <h1 className="page-title">경기 일정 · 결과</h1>
      <div className="tabs" role="tablist">
        {tabs.map((t) => (
          <button
            key={t.key}
            role="tab"
            aria-selected={tab === t.key}
            className={tab === t.key ? 'active' : ''}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>
      <div className="grid grid-3">
        {list.map((m) => (
          <MatchCard key={m.id} match={m} />
        ))}
      </div>
    </div>
  )
}
