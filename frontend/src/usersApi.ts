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

async function fetchJson<T>(path: string, errorMessage: string): Promise<T> {
  const response = await fetch(path)

  if (!response.ok) {
    throw new Error(errorMessage)
  }

  return (await response.json()) as T
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
