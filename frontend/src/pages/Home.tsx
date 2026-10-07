import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import MatchCard from '../components/MatchCard'
import { formatKickoff, matches, posts } from '../data/mock'

function useCountdown(target: string) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(t)
  }, [])
  const diff = Math.max(0, new Date(target).getTime() - now)
  return {
    days: Math.floor(diff / 86400000),
    hours: Math.floor(diff / 3600000) % 24,
    minutes: Math.floor(diff / 60000) % 60,
    seconds: Math.floor(diff / 1000) % 60,
  }
}

export default function Home() {
  const next = matches.filter((m) => m.status === 'SCHEDULED')[0]
  const recent = matches.filter((m) => m.status === 'FINISHED').slice(0, 3)
  const cd = useCountdown(next.kickoff)
  const units: Array<[string, number]> = [
    ['일', cd.days],
    ['시간', cd.hours],
    ['분', cd.minutes],
    ['초', cd.seconds],
  ]

  return (
    <>
      <section className="hero">
        <div className="container hero-inner">
          <p className="eyebrow">NEXT MATCH · {next.competition}</p>
          <h1 className="hero-title">
            {next.home} <span>vs</span> {next.away}
          </h1>
          <p className="hero-date">{formatKickoff(next.kickoff)}</p>
          <div className="countdown">
            {units.map(([label, value]) => (
              <div key={label} className="count-box">
                <strong>{String(value).padStart(2, '0')}</strong>
                <span>{label}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      <div className="container">
        <section className="section">
          <div className="section-head">
            <h2>최근 경기 결과</h2>
            <Link to="/matches">전체 보기</Link>
          </div>
          <div className="grid grid-3">
            {recent.map((m) => (
              <MatchCard key={m.id} match={m} />
            ))}
          </div>
        </section>

        <section className="section">
          <div className="section-head">
            <h2>인기 게시글</h2>
            <Link to="/board">게시판 가기</Link>
          </div>
          <ul className="card list">
            {[...posts]
              .sort((a, b) => b.likes - a.likes)
              .slice(0, 4)
              .map((p) => (
                <li key={p.id}>
                  <Link to="/board" className="list-title">
                    {p.title}
                  </Link>
                  <span className="muted">
                    {p.author} · 좋아요 {p.likes}
                  </span>
                </li>
              ))}
          </ul>
        </section>
      </div>
    </>
  )
}
