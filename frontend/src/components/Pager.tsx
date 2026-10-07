type Props = {
  page: number // 0부터 시작
  totalPages: number
  onChange: (page: number) => void
}

export default function Pager({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null

  // 현재 페이지 주변 5개만 보여 준다.
  const start = Math.max(0, Math.min(page - 2, totalPages - 5))
  const end = Math.min(totalPages, start + 5)
  const pages = Array.from({ length: end - start }, (_, i) => start + i)

  return (
    <nav className="pager" aria-label="페이지 이동">
      <button type="button" onClick={() => onChange(page - 1)} disabled={page === 0} aria-label="이전 페이지">
        ‹
      </button>
      {pages.map((p) => (
        <button
          key={p}
          type="button"
          className={p === page ? 'active' : ''}
          aria-current={p === page ? 'page' : undefined}
          onClick={() => onChange(p)}
        >
          {p + 1}
        </button>
      ))}
      <button type="button" onClick={() => onChange(page + 1)} disabled={page >= totalPages - 1} aria-label="다음 페이지">
        ›
      </button>
    </nav>
  )
}
