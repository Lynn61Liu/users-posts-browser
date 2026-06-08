import { describe, expect, it } from 'vitest'
import { resolveApiProxyTarget, resolveDevToolsEnabled } from './env'

describe('TC-1.4: frontend environment values are loaded correctly', () => {
  it('uses the local backend URL when no env override is provided', () => {
    expect(resolveApiProxyTarget({})).toBe('http://localhost:8080')
  })

  it('uses the provided env override when available', () => {
    expect(
      resolveApiProxyTarget({ VITE_API_PROXY_TARGET: 'http://backend:8080' }),
    ).toBe('http://backend:8080')
  })

  it('keeps dev tools hidden unless explicitly enabled', () => {
    expect(resolveDevToolsEnabled({})).toBe(false)
    expect(
      resolveDevToolsEnabled({ VITE_ENABLE_DEV_TOOLS: 'true' }),
    ).toBe(true)
  })
})
