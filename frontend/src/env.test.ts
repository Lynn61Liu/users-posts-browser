import { describe, expect, it } from 'vitest'
import { resolveApiProxyTarget } from './env'

describe('TC-1.4: frontend environment values are loaded correctly', () => {
  it('uses the local backend URL when no env override is provided', () => {
    expect(resolveApiProxyTarget({})).toBe('http://localhost:8080')
  })

  it('uses the provided env override when available', () => {
    expect(
      resolveApiProxyTarget({ VITE_API_PROXY_TARGET: 'http://backend:8080' }),
    ).toBe('http://backend:8080')
  })
})
