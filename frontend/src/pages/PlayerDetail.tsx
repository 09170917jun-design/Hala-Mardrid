import { useCallback } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getPlayer } from '../api/football'
import { useFetch } from '../api/useFetch'
import { ageOf, positionLabel } from '../utils/football'

export default function PlayerDetail() {
  const id = Number(useParams().id)
  const load = useCallback(() => getPlayer(id), [id])
  const { data: player, error, loading } = useFetch(load)

  if (loading) return <div className="container page muted">불러오는 중...</div>
  if (error || !player) {
    return (
      <div className="container page not-found">
        <h1 className="page-title">선수를 찾을 수 없어요</h1>
        <p className="muted">{error ?? '선수 정보가 없습니다.'}</p>
        <Link to="/players" className="btn btn-primary">
          선수단으로
        </Link>
      </div>
    )
  }

  return (
    <div className="container page">
      <p>
        <Link to="/players" className="muted">
          ← 선수단
        </Link>
      </p>
      <div className="card profile player-profile">
        <div className="player-number">{player.shirtNumber ?? player.position}</div>
        <h1 className="post-title">{player.name}</h1>
        <dl>
          <div>
            <dt>포지션</dt>
            <dd>{positionLabel[player.position]}</dd>
          </div>
          <div>
            <dt>국적</dt>
            <dd>{player.nationality ?? '-'}</dd>
          </div>
          <div>
            <dt>생년월일</dt>
            <dd>
              {player.dateOfBirth ? `${player.dateOfBirth} (만 ${ageOf(player.dateOfBirth)}세)` : '-'}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  )
}
