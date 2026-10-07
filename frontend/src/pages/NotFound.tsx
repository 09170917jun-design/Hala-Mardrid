import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="container page not-found">
      <h1 className="page-title">404</h1>
      <p className="muted">찾으시는 페이지가 없어요.</p>
      <Link to="/" className="btn btn-primary">
        홈으로
      </Link>
    </div>
  )
}
