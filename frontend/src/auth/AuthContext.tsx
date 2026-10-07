import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { api, setAccessToken, type AuthResponse, type User } from '../api/client'

type AuthContextValue = {
  user: User | null
  ready: boolean
  login: (email: string, password: string) => Promise<void>
  signup: (email: string, password: string, nickname: string) => Promise<void>
  kakaoLogin: (code: string, redirectUri: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

// 리프레시 토큰은 1회용이라, 동시에 여러 번 호출돼도(StrictMode 등) 요청은 한 번만 보낸다.
let refreshInFlight: Promise<AuthResponse> | null = null
const refreshOnce = () => {
  refreshInFlight ??= api<AuthResponse>('/api/auth/refresh', { method: 'POST' }).finally(() => {
    refreshInFlight = null
  })
  return refreshInFlight
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [ready, setReady] = useState(false)
  const timer = useRef<number | undefined>(undefined)

  const apply = useCallback(function applyAuth(res: AuthResponse | null) {
    window.clearTimeout(timer.current)
    setAccessToken(res?.accessToken ?? null)
    setUser(res?.user ?? null)
    if (res) {
      // 만료 1분 전에 미리 갱신한다.
      const delay = Math.max(10, res.expiresIn - 60) * 1000
      timer.current = window.setTimeout(() => {
        refreshOnce().then(applyAuth).catch(() => applyAuth(null))
      }, delay)
    }
  }, [])

  useEffect(() => {
    refreshOnce()
      .then(apply)
      .catch(() => apply(null))
      .finally(() => setReady(true))
    return () => window.clearTimeout(timer.current)
  }, [apply])

  const login = useCallback(
    async (email: string, password: string) => {
      apply(await api<AuthResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }))
    },
    [apply],
  )

  const signup = useCallback(
    async (email: string, password: string, nickname: string) => {
      apply(
        await api<AuthResponse>('/api/auth/signup', {
          method: 'POST',
          body: JSON.stringify({ email, password, nickname }),
        }),
      )
    },
    [apply],
  )

  const kakaoLogin = useCallback(
    async (code: string, redirectUri: string) => {
      apply(await api<AuthResponse>('/api/auth/kakao', { method: 'POST', body: JSON.stringify({ code, redirectUri }) }))
    },
    [apply],
  )

  const logout = useCallback(async () => {
    try {
      await api<void>('/api/auth/logout', { method: 'POST' })
    } finally {
      apply(null)
    }
  }, [apply])

  const value = useMemo(
    () => ({ user, ready, login, signup, kakaoLogin, logout }),
    [user, ready, login, signup, kakaoLogin, logout],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
