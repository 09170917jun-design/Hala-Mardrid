import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listPosts } from '../api/board'
import { listMatches, type MatchItem } from '../api/football'
import { useFetch } from '../api/useFetch'
import MatchCard from '../components/MatchCard'
import { competitionName, teamName } from '../utils/football'
import { formatKickoff } from '../utils/format'

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

function NextMatchHero({ match }: { match: MatchItem }) {
  const cd = useCountdown(match.kickoffAt)
  const live = match.status === 'LIVE'
  const units: Array<[string, number]> = [
    ['일', cd.days],
    ['시간', cd.hours],
    ['분', cd.minutes],
    ['초', cd.seconds],
  ]

  return (
    <div className="container hero-inner">
      <p className="eyebrow">
        {live ? 'LIVE' : 'NEXT MATCH'} · {competitionName(match.competitionCode, match.competitionName)}
      </p>
      <h1 className="hero-title">
        {teamName(match.homeTeam)} <span>vs</span> {teamName(match.awayTeam)}
      </h1>
      <p className="hero-date">{formatKickoff(match.kickoffAt)}</p>
      {live ? (
        <p className="hero-live">
          {match.homeScore ?? 0} : {match.awayScore ?? 0}
        </p>
      ) : (
        <div className="countdown">
          {units.map(([label, value]) => (
            <div key={label} className="count-box">
              <strong>{String(value).padStart(2, '0')}</strong>
              <span>{label}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default function Home() {
  const loadNext = useCallback(() => listMatches('upcoming', 1), [])
  const loadRecent = useCallback(() => listMatches('finished', 3), [])
  const loadPopular = useCallback(() => listPosts(0, 4, 'popular'), [])
  const next = useFetch(loadNext)
  const recent = useFetch(loadRecent)
  const popular = useFetch(loadPopular)
  const nextMatch = next.data?.[0]

  return (
    <>
      <section className="hero">
        {nextMatch ? (
          <NextMatchHero match={nextMatch} />
        ) : (
          <div className="container hero-inner">
            <p className="eyebrow">HALA MADRID</p>
            <h1 className="hero-title">레알 마드리드 팬 커뮤니티</h1>
            <p className="hero-date">
              {next.loading ? '경기 일정을 불러오는 중이에요...' : '다음 경기 일정을 준비하고 있어요.'}
            </p>
          </div>
        )}
      </section>

      <div className="container">
        <section className="section">
          <div className="section-head">
            <h2>최근 경기 결과</h2>
            <Link to="/matches">전체 보기</Link>
          </div>
          {recent.loading && <p className="muted">불러오는 중...</p>}
          {recent.error && <p className="muted">경기 결과를 불러오지 못했어요.</p>}
          {recent.data && recent.data.length === 0 && (
            <div className="card empty">
              <p>아직 종료된 경기가 없어요.</p>
            </div>
          )}
          {recent.data && recent.data.length > 0 && (
            <div className="grid grid-3">
              {recent.data.map((m) => (
                <MatchCard key={m.id} match={m} />
              ))}
            </div>
          )}
        </section>

        <section className="section">
          <div className="section-head">
            <h2>인기 게시글</h2>
            <Link to="/board">게시판 가기</Link>
          </div>
          {popular.loading && <p className="muted">불러오는 중...</p>}
          {popular.error && <p className="muted">인기 글을 불러오지 못했어요.</p>}
          {popular.data && popular.data.items.length === 0 && (
            <div className="card empty">
              <p>
                아직 글이 없어요. <Link to="/board/write">첫 글을 남겨 보세요!</Link>
              </p>
            </div>
          )}
          {popular.data && popular.data.items.length > 0 && (
            <ul className="card list">
              {popular.data.items.map((p) => (
                <li key={p.id}>
                  <Link to={`/board/${p.id}`} className="list-title">
                    {p.title}
                  </Link>
                  <span className="muted">
                    {p.authorNickname} · 좋아요 {p.likeCount}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </>
  )
}
