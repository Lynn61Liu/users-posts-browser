export type ApiEnv = {
  VITE_API_PROXY_TARGET?: string
}

export function resolveApiProxyTarget(env: ApiEnv = {}): string {
  return env.VITE_API_PROXY_TARGET ?? 'http://localhost:8080'
}
