import { describe, expect, it } from 'vitest'
import {
  resolveApiProxyTarget,
  resolveAppVersion,
  resolveDeployEnvironment,
  resolveDevToolsEnabled,
} from './env'

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

  it('resolves deployment labels for blue/green builds', () => {
    expect(resolveAppVersion({})).toBe('Local Development')
    expect(resolveDeployEnvironment({})).toBe('Local environment')
    expect(
      resolveAppVersion({ VITE_APP_VERSION: 'Version 2.0 - New Feature Deployed' }),
    ).toBe('Version 2.0 - New Feature Deployed')
    expect(resolveDeployEnvironment({ VITE_DEPLOY_ENV: 'Green environment' })).toBe(
      'Green environment',
    )
  })
})
