import { useEffect, useState } from 'react'
import { ApiError } from './client'

export const errorMessage = (err: unknown) =>
  err instanceof ApiError ? err.message : '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.'

/**
 * 데이터를 불러온다. `load`가 바뀌면(useCallback으로 감싼 함수의 의존값이 바뀌면) 다시 불러온다.
 * 새로 불러오는 동안에는 loading이 true이고, `reload()`로 같은 요청을 다시 보낼 수 있다.
 */
export function useFetch<T>(load: () => Promise<T>) {
  const [state, setState] = useState<{ source: unknown; data?: T; error?: string }>({ source: null })
  const [tick, setTick] = useState(0)

  useEffect(() => {
    let cancelled = false
    load()
      .then((data) => {
        if (!cancelled) setState({ source: load, data })
      })
      .catch((err) => {
        if (!cancelled) setState({ source: load, error: errorMessage(err) })
      })
    return () => {
      cancelled = true
    }
  }, [load, tick])

  const loading = state.source !== load
  return {
    data: loading ? undefined : state.data,
    error: loading ? undefined : state.error,
    loading,
    reload: () => setTick((t) => t + 1),
  }
}
