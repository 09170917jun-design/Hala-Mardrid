import { api } from '../api/client'

const STATE_KEY = 'kakao_oauth_state'
const FROM_KEY = 'kakao_oauth_from'

export const kakaoRedirectUri = () => `${window.location.origin}/auth/kakao/callback`

/** 카카오 인증 페이지로 이동한다. CSRF 방지를 위해 무작위 state를 만들어 세션에 저장한다. */
export async function startKakaoLogin(from: string) {
  const state = crypto.randomUUID()
  sessionStorage.setItem(STATE_KEY, state)
  sessionStorage.setItem(FROM_KEY, from)
  const { url } = await api<{ url: string }>(
    `/api/auth/kakao/url?redirectUri=${encodeURIComponent(kakaoRedirectUri())}&state=${encodeURIComponent(state)}`,
  )
  window.location.assign(url)
}

/** 콜백으로 돌아온 state가 우리가 보낸 값과 같은지 확인한다. (저장값은 지우지 않는다) */
export function isValidKakaoState(returnedState: string | null): boolean {
  const saved = sessionStorage.getItem(STATE_KEY)
  return !!saved && saved === returnedState
}

/** 로그인 후 돌아갈 경로를 반환하고 임시 저장값을 지운다. */
export function finishKakaoState(): string {
  const from = sessionStorage.getItem(FROM_KEY) ?? '/'
  sessionStorage.removeItem(STATE_KEY)
  sessionStorage.removeItem(FROM_KEY)
  return from
}
