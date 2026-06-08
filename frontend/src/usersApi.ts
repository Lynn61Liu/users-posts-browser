export type UserSummary = {
  id: number
  externalId: number
  name: string
  username: string
  email: string
  companyName: string
}

export type UserDetail = {
  id: number
  externalId: number
  name: string
  username: string
  email: string
  phone: string
  website: string
  address: {
    street: string
    suite: string
    city: string
    zipcode: string
    geo: {
      lat: string
      lng: string
    }
  }
  company: {
    name: string
    catchPhrase: string
    bs: string
  }
}

export type UserPost = {
  id: number
  externalId: number
  title: string
  body: string
}

export type ApiErrorKind = 'network' | 'http' | 'parse'

export class ApiError extends Error {
  readonly kind: ApiErrorKind
  readonly status?: number
  readonly responseBody?: string | null

  constructor(
    kind: ApiErrorKind,
    message: string,
    options: { status?: number; responseBody?: string | null } = {},
  ) {
    super(message)
    this.name = 'ApiError'
    this.kind = kind
    this.status = options.status
    this.responseBody = options.responseBody
  }
}

async function fetchJson<T>(path: string, errorMessage: string): Promise<T> {
  let response: Response

  try {
    response = await fetch(path)
  } catch {
    throw new ApiError('network', errorMessage)
  }

  if (!response.ok) {
    throw new ApiError('http', errorMessage, {
      status: response.status,
      responseBody: (await readResponseBody(response)) ?? undefined,
    })
  }

  try {
    return (await response.json()) as T
  } catch {
    throw new ApiError('parse', errorMessage)
  }
}

async function readResponseBody(response: Response): Promise<string | null> {
  try {
    const body = await response.text()
    return body.trim() === '' ? null : body.trim()
  } catch {
    return null
  }
}

export function fetchUsers(): Promise<UserSummary[]> {
  return fetchJson<UserSummary[]>('/api/users', 'Could not load users.')
}

export function fetchUser(userId: number): Promise<UserDetail> {
  return fetchJson<UserDetail>(
    `/api/users/${userId}`,
    'Could not load the selected user.',
  )
}

export function fetchUserPosts(userId: number): Promise<UserPost[]> {
  return fetchJson<UserPost[]>(
    `/api/users/${userId}/posts`,
    'Could not load the selected user.',
  )
}
