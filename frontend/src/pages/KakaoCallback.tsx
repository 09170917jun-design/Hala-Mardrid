import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { finishKakaoState, isValidKakaoState, kakaoRedirectUri } from '../auth/kakao'

// 인가 코드는 1회용이라, StrictMode에서 effect가 두 번 실행돼도 요청은 한 번만 보낸다.
const handled = new Set<string>()

export default function KakaoCallback() {
  const { kakaoLogin } = useAuth()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const code = params.get('code')
  const state = params.get('state')
  const kakaoError = params.get('error')

  // 처음 렌더링할 때의 state 검증 결과를 고정해 둔다. (아래 effect가 저장값을 지운 뒤에도 바뀌지 않도록)
  const [stateOk] = useState(() => isValidKakaoState(state))
  const [requestError, setRequestError] = useState<string | null>(null)

  let error: string | null = requestError
  if (kakaoError) {
    // 사용자가 동의 화면에서 취소한 경우 등
    error = kakaoError === 'access_denied' ? '카카오 로그인이 취소되었습니다.' : '카카오 로그인에 실패했습니다.'
  } else if (!code) {
    error = '카카오 로그인에 실패했습니다.'
  } else if (!stateOk) {
    error = '잘못된 로그인 요청입니다. 처음부터 다시 시도해 주세요.'
  }

  useEffect(() => {
    if (kakaoError || !code || !stateOk || handled.has(code)) return
    handled.add(code)
    const from = finishKakaoState()

    kakaoLogin(code, kakaoRedirectUri())
      .then(() => navigate(from, { replace: true }))
      .catch((err) =>
        setRequestError(err instanceof ApiError ? err.message : '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.'),
      )
  }, [code, kakaoError, stateOk, kakaoLogin, navigate])

  return (
    <div className="container page not-found">
      {error ? (
        <>
          <h1 className="page-title">로그인하지 못했어요</h1>
          <p className="muted">{error}</p>
          <Link to="/login" className="btn btn-primary">
            로그인 화면으로
          </Link>
        </>
      ) : (
        <p className="muted">카카오 로그인 중...</p>
      )}
    </div>
  )
}
