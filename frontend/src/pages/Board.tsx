import { useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { listPosts, type PostSort } from '../api/board'
import { useFetch } from '../api/useFetch'
import Pager from '../components/Pager'
import { formatListDate } from '../utils/format'

const sorts: Array<{ key: PostSort; label: string }> = [
  { key: 'latest', label: '최신순' },
  { key: 'popular', label: '인기순' },
]

export default function Board() {
  const [params, setParams] = useSearchParams()
  const page = Math.max(0, (Number(params.get('page')) || 1) - 1)
  const sort: PostSort = params.get('sort') === 'popular' ? 'popular' : 'latest'

  const load = useCallback(() => listPosts(page, 10, sort), [page, sort])
  const { data, error, loading } = useFetch(load)

  const go = (nextPage: number, nextSort: PostSort = sort) => {
    const next = new URLSearchParams()
    if (nextPage > 0) next.set('page', String(nextPage + 1))
    if (nextSort !== 'latest') next.set('sort', nextSort)
    setParams(next)
  }

  return (
    <div className="container page">
      <div className="section-head">
        <h1 className="page-title">자유게시판</h1>
        <Link to="/board/write" className="btn btn-primary">
          글쓰기
        </Link>
      </div>

      <div className="tabs">
        {sorts.map((s) => (
          <button key={s.key} type="button" className={sort === s.key ? 'active' : ''} onClick={() => go(0, s.key)}>
            {s.label}
          </button>
        ))}
      </div>

      {loading && <p className="muted">불러오는 중... (서버가 잠들어 있었다면 1분쯤 걸릴 수 있어요)</p>}
      {error && <p className="form-error">{error}</p>}
      {data && data.items.length === 0 && (
        <div className="card empty">
          <p>아직 글이 없어요. 첫 글을 남겨 보세요!</p>
        </div>
      )}
      {data && data.items.length > 0 && (
        <ul className="card list board-list">
          {data.items.map((p) => (
            <li key={p.id}>
              <Link to={`/board/${p.id}`} className="board-item">
                <span className="list-title">
                  {p.title}
                  {p.commentCount > 0 && <span className="comment-badge">[{p.commentCount}]</span>}
                </span>
                <span className="muted">
                  {p.authorNickname} · {formatListDate(p.createdAt)} · 조회 {p.viewCount}
                </span>
              </Link>
              <div className="board-stats">
                <span>♥ {p.likeCount}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
      {data && <Pager page={data.page} totalPages={data.totalPages} onChange={(p) => go(p)} />}
    </div>
  )
}
