import { useCallback, useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { createPost, getPost, updatePost, type PostDetail } from '../api/board'
import { errorMessage, useFetch } from '../api/useFetch'
import { useAuth } from '../auth/AuthContext'

/** 글쓰기(/board/write)와 수정(/board/:id/edit)에 함께 쓰는 화면. */
export default function PostEdit() {
  const { user, ready } = useAuth()
  const params = useParams()
  const editId = params.id ? Number(params.id) : null
  const location = editId ? `/board/${editId}/edit` : '/board/write'

  const loadExisting = useCallback(() => (editId ? getPost(editId) : Promise.resolve(null)), [editId])
  const { data: existing, error, loading } = useFetch(loadExisting)

  if (!ready || loading) return <div className="container page muted">불러오는 중...</div>
  if (!user) return <Navigate to="/login" state={{ from: location }} replace />
  if (error) {
    return (
      <div className="container page not-found">
        <p className="muted">{error}</p>
        <Link to="/board" className="btn btn-primary">
          목록으로
        </Link>
      </div>
    )
  }
  if (existing && existing.authorId !== user.id) return <Navigate to={`/board/${existing.id}`} replace />

  return <PostForm existing={existing ?? null} />
}

function PostForm({ existing }: { existing: PostDetail | null }) {
  const navigate = useNavigate()
  const [title, setTitle] = useState(existing?.title ?? '')
  const [content, setContent] = useState(existing?.content ?? '')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const saved = existing ? await updatePost(existing.id, title, content) : await createPost(title, content)
      navigate(`/board/${saved.id}`, { replace: true })
    } catch (err) {
      setError(errorMessage(err))
      setSubmitting(false)
    }
  }

  return (
    <div className="container page">
      <h1 className="page-title">{existing ? '글 수정' : '글쓰기'}</h1>
      <form className="card form post-form" onSubmit={onSubmit}>
        <label>
          제목
          <input
            type="text"
            maxLength={100}
            placeholder="제목을 입력하세요"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </label>
        <label>
          내용
          <textarea
            rows={12}
            maxLength={5000}
            placeholder="내용을 입력하세요 (최대 5000자)"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
        </label>
        <p className="muted post-count">{content.length} / 5000</p>
        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}
        <div className="form-buttons">
          <Link to={existing ? `/board/${existing.id}` : '/board'} className="btn btn-ghost">
            취소
          </Link>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? '저장 중...' : existing ? '수정하기' : '등록하기'}
          </button>
        </div>
      </form>
    </div>
  )
}
