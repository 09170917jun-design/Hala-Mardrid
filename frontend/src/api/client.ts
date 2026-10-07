export type User = {
  id: number
  email: string | null
  nickname: string
  role: 'USER' | 'ADMIN'
}

export type AuthResponse = {
  accessToken: string
  expiresIn: number
  user: User
}

export class ApiError extends Error {
  status: number
  code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

// 액세스 토큰은 메모리에만 둔다. (새로고침하면 refresh 쿠키로 다시 발급받는다)
let accessToken: string | null = null

export const setAccessToken = (token: string | null) => {
  accessToken = token
}

async function parseError(res: Response): Promise<ApiError> {
  try {
    const data = (await res.json()) as { code?: string; message?: string }
    return new ApiError(res.status, data.code ?? 'ERROR', data.message ?? '요청에 실패했습니다.')
  } catch {
    return new ApiError(res.status, 'ERROR', '요청에 실패했습니다.')
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)

  const res = await fetch(path, { ...init, headers, credentials: 'same-origin' })
  if (!res.ok) throw await parseError(res)
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}
