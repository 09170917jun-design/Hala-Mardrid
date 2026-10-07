import { formatKickoff, type Match } from '../data/mock'

export default function MatchCard({ match }: { match: Match }) {
  const finished = match.status === 'FINISHED'
  return (
    <article className="card match-card">
      <div className="match-meta">
        <span className="badge">{match.competition}</span>
        <span className="muted">{formatKickoff(match.kickoff)}</span>
      </div>
      <div className="match-teams">
        <span className="team">{match.home}</span>
        <span className="score">{finished ? `${match.homeScore} : ${match.awayScore}` : 'vs'}</span>
        <span className="team">{match.away}</span>
      </div>
      {finished && <p className="match-action">선수 평점 투표 &rarr;</p>}
    </article>
  )
}
