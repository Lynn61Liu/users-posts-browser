export type ApiEnv = {
  VITE_API_PROXY_TARGET?: string
  VITE_ENABLE_DEV_TOOLS?: string
}

export function resolveApiProxyTarget(env: ApiEnv = {}): string {
  return env.VITE_API_PROXY_TARGET ?? 'http://localhost:8080'
}

export function resolveDevToolsEnabled(env: ApiEnv = {}): boolean {
  return env.VITE_ENABLE_DEV_TOOLS === 'true'
}
