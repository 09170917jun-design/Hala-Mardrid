import type { MatchItem } from '../api/football'
import { competitionName, isRealMadrid, teamName } from '../utils/football'
import { formatKickoff } from '../utils/format'

const statusLabel: Partial<Record<MatchItem['status'], string>> = {
  LIVE: '진행 중',
  POSTPONED: '연기',
  CANCELLED: '취소',
}

export default function MatchCard({ match }: { match: MatchItem }) {
  const finished = match.status === 'FINISHED'
  const extra = statusLabel[match.status]
  const hasScore = (finished || match.status === 'LIVE') && match.homeScore !== null && match.awayScore !== null

  return (
    <article className="card match-card">
      <div className="match-meta">
        <span className="badge">
          {competitionName(match.competitionCode, match.competitionName)}
          {match.matchday ? ` ${match.matchday}R` : ''}
        </span>
        <span className="muted">{formatKickoff(match.kickoffAt)}</span>
      </div>
      <div className="match-teams">
        <span className={`team ${isRealMadrid(match.homeTeam) ? 'team-us' : ''}`}>{teamName(match.homeTeam)}</span>
        <span className="score">{hasScore ? `${match.homeScore} : ${match.awayScore}` : 'vs'}</span>
        <span className={`team ${isRealMadrid(match.awayTeam) ? 'team-us' : ''}`}>{teamName(match.awayTeam)}</span>
      </div>
      {extra && <p className="match-state">{extra}</p>}
    </article>
  )
}
