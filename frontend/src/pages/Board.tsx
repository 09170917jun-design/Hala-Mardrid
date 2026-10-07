import { Link } from 'react-router-dom'
import { posts } from '../data/mock'

export default function Board() {
  return (
    <div className="container page">
      <div className="section-head">
        <h1 className="page-title">자유게시판</h1>
        <Link to="/login" className="btn btn-primary">
          글쓰기
        </Link>
      </div>
      <ul className="card list board-list">
        {posts.map((p) => (
          <li key={p.id}>
            <div>
              <span className="list-title">{p.title}</span>
              <span className="muted">
                {p.author} · {p.createdAt} · 조회 {p.views}
              </span>
            </div>
            <div className="board-stats">
              <span>♥ {p.likes}</span>
              <span>댓글 {p.comments}</span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
