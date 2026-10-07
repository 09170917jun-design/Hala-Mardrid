import { useCallback, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createComment,
  deleteComment,
  deletePost,
  getPost,
  listComments,
  reportPost,
  toggleLike,
  type PostDetail as PostDetailData,
} from '../api/board'
import { errorMessage, useFetch } from '../api/useFetch'
import { useAuth } from '../auth/AuthContext'
import { formatDateTime } from '../utils/format'

export default function PostDetail() {
  const id = Number(useParams().id)
  const loadPost = useCallback(() => getPost(id), [id])
  const { data: post, error, loading } = useFetch(loadPost)

  if (loading) return <div className="container page muted">불러오는 중...</div>
  if (error || !post) {
    return (
      <div className="container page not-found">
        <h1 className="page-title">글을 불러오지 못했어요</h1>
        <p className="muted">{error ?? '글을 찾을 수 없습니다.'}</p>
        <Link to="/board" className="btn btn-primary">
          목록으로
        </Link>
      </div>
    )
  }
  return <PostView post={post} />
}

function PostView({ post }: { post: PostDetailData }) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const isAuthor = user?.id === post.authorId
  const isAdmin = user?.role === 'ADMIN'

  const [like, setLike] = useState({ liked: post.likedByMe, count: post.likeCount })
  const [notice, setNotice] = useState<string | null>(null)
  const [reporting, setReporting] = useState(false)
  const [reason, setReason] = useState('')

  const requireLogin = () => {
    navigate('/login', { state: { from: `/board/${post.id}` } })
  }

  const onLike = async () => {
    if (!user) return requireLogin()
    try {
      const res = await toggleLike(post.id)
      setLike({ liked: res.liked, count: res.likeCount })
    } catch (err) {
      setNotice(errorMessage(err))
    }
  }

  const onDelete = async () => {
    if (!window.confirm('이 글을 삭제할까요?')) return
    try {
      await deletePost(post.id)
      navigate('/board', { replace: true })
    } catch (err) {
      setNotice(errorMessage(err))
    }
  }

  const onReport = async (e: FormEvent) => {
    e.preventDefault()
    try {
      await reportPost(post.id, reason)
      setReporting(false)
      setReason('')
      setNotice('신고가 접수되었습니다. 확인 후 조치하겠습니다.')
    } catch (err) {
      setNotice(errorMessage(err))
    }
  }

  return (
    <div className="container page">
      <p>
        <Link to="/board" className="muted">
          ← 게시판
        </Link>
      </p>
      <article className="card post">
        <h1 className="post-title">{post.title}</h1>
        <p className="muted post-meta">
          {post.authorNickname} · {formatDateTime(post.createdAt)} · 조회 {post.viewCount}
          {post.updatedAt !== post.createdAt && ' · 수정됨'}
        </p>
        <div className="post-body">{post.content}</div>

        <div className="post-actions">
          <button type="button" className={`btn btn-soft like-btn ${like.liked ? 'liked' : ''}`} onClick={onLike}>
            {like.liked ? '♥' : '♡'} 좋아요 {like.count}
          </button>
          <div className="post-actions-right">
            {isAuthor && (
              <Link to={`/board/${post.id}/edit`} className="btn btn-ghost">
                수정
              </Link>
            )}
            {(isAuthor || isAdmin) && (
              <button type="button" className="btn btn-ghost" onClick={onDelete}>
                삭제
              </button>
            )}
            {user && !isAuthor && (
              <button type="button" className="btn btn-ghost" onClick={() => setReporting((v) => !v)}>
                신고
              </button>
            )}
          </div>
        </div>

        {reporting && (
          <form className="form report-form" onSubmit={onReport}>
            <label>
              신고 사유
              <input
                type="text"
                maxLength={200}
                placeholder="예: 광고성 글입니다"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                required
              />
            </label>
            <button type="submit" className="btn btn-primary">
              신고하기
            </button>
          </form>
        )}
        {notice && <p className="form-notice">{notice}</p>}
      </article>

      <Comments postId={post.id} />
    </div>
  )
}

function Comments({ postId }: { postId: number }) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const load = useCallback(() => listComments(postId), [postId])
  const { data: comments, error, reload } = useFetch(load)

  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)
    setSubmitting(true)
    try {
      await createComment(postId, content)
      setContent('')
      reload()
    } catch (err) {
      setFormError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  const onDelete = async (commentId: number) => {
    if (!window.confirm('이 댓글을 삭제할까요?')) return
    try {
      await deleteComment(commentId)
      reload()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  return (
    <section className="section">
      <h2 className="comments-title">댓글 {comments ? comments.length : ''}</h2>
      {error && <p className="form-error">{error}</p>}
      <ul className="card list comment-list">
        {comments?.length === 0 && <li className="muted">첫 댓글을 남겨 보세요.</li>}
        {comments?.map((c) => (
          <li key={c.id}>
            <div className="comment-main">
              <p className="comment-head">
                <strong>{c.authorNickname}</strong>
                <span className="muted">{formatDateTime(c.createdAt)}</span>
              </p>
              <p className="comment-text">{c.content}</p>
            </div>
            {(user?.id === c.authorId || user?.role === 'ADMIN') && (
              <button type="button" className="link-btn muted-link" onClick={() => onDelete(c.id)}>
                삭제
              </button>
            )}
          </li>
        ))}
      </ul>

      {user ? (
        <form className="form comment-form" onSubmit={onSubmit}>
          <textarea
            placeholder="댓글을 입력하세요"
            maxLength={500}
            rows={3}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
          {formError && (
            <p className="form-error" role="alert">
              {formError}
            </p>
          )}
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? '등록 중...' : '댓글 등록'}
          </button>
        </form>
      ) : (
        <p className="muted comment-login">
          댓글을 쓰려면{' '}
          <button
            type="button"
            className="link-btn"
            onClick={() => navigate('/login', { state: { from: `/board/${postId}` } })}
          >
            로그인
          </button>
          해 주세요.
        </p>
      )}
    </section>
  )
}
