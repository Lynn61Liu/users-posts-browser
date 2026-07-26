export type ApiEnv = {
  VITE_API_PROXY_TARGET?: string
  VITE_ENABLE_DEV_TOOLS?: string
  VITE_APP_VERSION?: string
  VITE_DEPLOY_ENV?: string
}

export function resolveApiProxyTarget(env: ApiEnv = {}): string {
  return env.VITE_API_PROXY_TARGET ?? 'http://localhost:8080'
}

export function resolveDevToolsEnabled(env: ApiEnv = {}): boolean {
  return env.VITE_ENABLE_DEV_TOOLS === 'true'
}

export function resolveAppVersion(env: ApiEnv = {}): string {
  return env.VITE_APP_VERSION ?? 'Local Development'
}

export function resolveDeployEnvironment(env: ApiEnv = {}): string {
  return env.VITE_DEPLOY_ENV ?? 'Local environment'
}
