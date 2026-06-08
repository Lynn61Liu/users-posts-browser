import { useEffect, useState } from 'react'
import { AppHeader } from './components/layout/AppHeader'
import { resolveDevToolsEnabled } from './env'
import { ApiError, fetchUser, fetchUserPosts, fetchUsers, type UserDetail, type UserPost, type UserSummary } from './usersApi'
import { UserListPanel } from './components/users/UserListPanel'
import { UserDetailPanel } from './components/users/UserDetailPanel'
import type { SyncStatusTone } from './components/layout/AppHeader'

type AppEnv = {
  VITE_ENABLE_DEV_TOOLS?: string
}

type AppProps = {
  env?: AppEnv
}

type UsersLoadState = 'loading' | 'ready' | 'empty' | 'error'
type DetailLoadState = 'idle' | 'loading' | 'ready' | 'empty' | 'error'
type SyncMessage = {
  kind: SyncStatusTone
  label: string
  detail: string
}

type FetchFailureMessage = {
  title: string
  detail: string
}

function App({
  env = { VITE_ENABLE_DEV_TOOLS: import.meta.env.VITE_ENABLE_DEV_TOOLS },
}: AppProps) {
  const devToolsEnabled = resolveDevToolsEnabled(env)
  const [users, setUsers] = useState<UserSummary[]>([])
  const [usersLoadState, setUsersLoadState] =
    useState<UsersLoadState>('loading')
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const [selectedUser, setSelectedUser] = useState<UserDetail | null>(null)
  const [selectedUserPosts, setSelectedUserPosts] = useState<UserPost[]>([])
  const [detailState, setDetailState] = useState<DetailLoadState>('idle')
  const [postPage, setPostPage] = useState(1)
  const [syncMessage, setSyncMessage] = useState<SyncMessage>({
    kind: 'ready',
    label: 'Ready to sync',
    detail: 'Use the sync action to import fresh data.',
  })
  const [lastSyncedAt, setLastSyncedAt] = useState<Date | null>(null)
  const [isSyncing, setIsSyncing] = useState(false)
  const [resetStatus, setResetStatus] = useState<string | null>(null)
  const [isResetting, setIsResetting] = useState(false)
  const [dataVersion, setDataVersion] = useState(0)
  const [usersLoadError, setUsersLoadError] = useState<FetchFailureMessage | null>(null)
  const [detailLoadError, setDetailLoadError] = useState<FetchFailureMessage | null>(null)

  const visibleUsers = users.slice(0, 10)

  function setDataFetchError(error: FetchFailureMessage) {
    setSyncMessage({
      kind: 'error',
      label: error.title,
      detail: error.detail,
    })
  }

  function classifyFetchFailure(
    error: unknown,
    subject: string,
  ): FetchFailureMessage {
    if (error instanceof ApiError) {
      if (error.kind === 'network') {
        return {
          title: 'Network error',
          detail: `Could not reach the backend while loading ${subject}. Check your connection and try again.`,
        }
      }

      if (error.kind === 'http') {
        const status = error.status ?? 500
        return {
          title: status === 500 ? 'Backend returned 500' : `Backend returned ${status}`,
          detail: `The backend returned ${status} while loading ${subject}. Try again or check the backend logs.`,
        }
      }

      return {
        title: 'Data fetch error',
        detail: `The backend response could not be parsed while loading ${subject}. Try again or check the backend logs.`,
      }
    }

    return {
      title: 'Data fetch error',
      detail: `Could not load ${subject} from the backend.`,
    }
  }

  useEffect(() => {
    let cancelled = false

    async function loadUsers() {
      setUsersLoadState('loading')
      setUsersLoadError(null)

      try {
        const importedUsers = await fetchUsers()

        if (cancelled) {
          return
        }

        setUsers(importedUsers)

        if (importedUsers.length === 0) {
        setUsersLoadState('empty')
        setSelectedUserId(null)
        setSelectedUser(null)
        setSelectedUserPosts([])
        setDetailState('empty')
          return
        }

        setUsersLoadState('ready')
        setSelectedUserId((current) =>
          current !== null && importedUsers.some((user) => user.id === current)
            ? current
            : importedUsers[0].id,
        )
      } catch (error) {
        if (cancelled) {
          return
        }

        setUsersLoadState('error')
        setSelectedUserId(null)
        setSelectedUser(null)
        setSelectedUserPosts([])
        setDetailState('empty')
        const failure = classifyFetchFailure(error, 'the users list')
        setUsersLoadError(failure)
        setDataFetchError(failure)
      }
    }

    void loadUsers()

    return () => {
      cancelled = true
    }
  }, [dataVersion])

  useEffect(() => {
    if (selectedUserId === null) {
      return
    }

    let cancelled = false
    const userId = selectedUserId

    async function loadSelectedUser() {
      setDetailState('loading')

      try {
        const [user, posts] = await Promise.all([
          fetchUser(userId),
          fetchUserPosts(userId),
        ])

        if (cancelled) {
          return
        }

        setSelectedUser(user)
        setSelectedUserPosts(posts)
        setDetailState('ready')
        setDetailLoadError(null)
        setPostPage(1)
      } catch (error) {
        if (cancelled) {
          return
        }

        setSelectedUser(null)
        setSelectedUserPosts([])
        setDetailState('error')
        const failure = classifyFetchFailure(error, 'the selected user')
        setDetailLoadError(failure)
        setDataFetchError(failure)
      }
    }

    void loadSelectedUser()

    return () => {
      cancelled = true
    }
  }, [dataVersion, selectedUserId])

  const syncStatusLabel = syncMessage.label
  const syncDescription = buildSyncDescription(syncMessage, lastSyncedAt)

  async function handleSync() {
    setIsSyncing(true)
    setSyncMessage({
      kind: 'syncing',
      label: 'Syncing data',
      detail: 'Fetching users and posts from JSONPlaceholder.',
    })

    try {
      const response = await fetch('/api/sync', {
        method: 'POST',
      })

      if (!response.ok) {
        throw new Error(`Sync failed with status ${response.status}`)
      }

      const payload = (await response.json()) as {
        status?: string
        message?: string
      }

      const now = new Date()
      setLastSyncedAt(now)
      setDataVersion((version) => version + 1)
      setSyncMessage({
        kind: syncKindForStatus(payload.status ?? 'success'),
        label: syncLabelForStatus(payload.status ?? 'success'),
        detail:
          payload.message ??
          'Imported data is available for browsing in the panels below.',
      })
    } catch {
      setSyncMessage({
        kind: 'error',
        label: 'Sync failed',
        detail: 'Check the backend logs and try again.',
      })
    } finally {
      setIsSyncing(false)
    }
  }

  async function handleResetDatabase() {
    setIsResetting(true)
    setResetStatus('Resetting development database...')

    try {
      const response = await fetch('/api/dev/reset', {
        method: 'POST',
      })

      if (!response.ok) {
        throw new Error(`Reset failed with status ${response.status}`)
      }

      const payload = (await response.json()) as {
        message?: string
      }

      setResetStatus(
        payload.message ?? 'Development database reset successfully.',
      )
    } catch {
      setResetStatus('Reset failed. Check the backend logs and try again.')
    } finally {
      setIsResetting(false)
    }
  }

  function handleSelectUser(userId: number) {
    setSelectedUserId(userId)
    setSelectedUser(null)
    setSelectedUserPosts([])
    setDetailState('loading')
    setDetailLoadError(null)
    setPostPage(1)
  }

  function handleSelectPostPage(page: number) {
    setPostPage(page)
  }

  return (
    <main className="app-shell flex min-h-screen flex-col">
      <AppHeader
        syncStatusLabel={syncStatusLabel}
        syncDescription={syncDescription}
        statusKind={syncMessage.kind}
        onSync={handleSync}
        isSyncing={isSyncing}
        devToolsEnabled={devToolsEnabled}
        onResetDatabase={devToolsEnabled ? handleResetDatabase : undefined}
        isResetting={isResetting}
        resetStatus={resetStatus}
      />

      <section className="mt-6 grid flex-1 min-h-0 gap-6 xl:grid-cols-[minmax(340px,440px)_minmax(0,1fr)]">
        <UserListPanel
          users={users}
          visibleUsers={visibleUsers}
          selectedUserId={selectedUserId}
        onSelectUser={handleSelectUser}
        isLoading={usersLoadState === 'loading'}
        isError={usersLoadState === 'error'}
        isEmpty={usersLoadState === 'empty'}
        errorTitle={usersLoadError?.title}
        errorDescription={usersLoadError?.detail}
      />

        <div
          key={`detail-${selectedUserId ?? 'none'}`}
          className="animate-panel-enter flex min-h-0 flex-1 flex-col gap-6"
        >
          <UserDetailPanel
            user={selectedUser}
            posts={selectedUserPosts}
            isLoading={detailState === 'loading'}
            isError={detailState === 'error'}
            isEmpty={detailState === 'empty'}
            errorTitle={detailLoadError?.title}
            errorDescription={detailLoadError?.detail}
            postPage={postPage}
            onPrevPostPage={() =>
              setPostPage((page) => Math.max(1, page - 1))
            }
            onNextPostPage={() => {
              const totalPages = Math.max(
                1,
                Math.ceil(selectedUserPosts.length / 6),
              )
              setPostPage((page) => Math.min(totalPages, page + 1))
            }}
            onSelectPostPage={handleSelectPostPage}
          />
        </div>
      </section>
    </main>
  )
}

function formatSyncTimestamp(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function buildSyncDescription(
  syncMessage: SyncMessage,
  lastSyncedAt: Date | null,
) {
  const showLastSyncedAt =
    lastSyncedAt &&
    (syncMessage.kind === 'success' ||
      syncMessage.kind === 'no_change' ||
      syncMessage.kind === 'update')

  if (!showLastSyncedAt) {
    return syncMessage.detail
  }

  return `${syncMessage.detail} Last synced: ${formatSyncTimestamp(lastSyncedAt)}.`
}

function syncLabelForStatus(status: string) {
  if (status === 'no_change') {
    return 'No changes'
  }

  if (status === 'update') {
    return 'Data updated'
  }

  if (status === 'error') {
    return 'Sync error'
  }

  return 'Sync complete'
}

function syncKindForStatus(status: string): SyncStatusTone {
  if (status === 'no_change') {
    return 'no_change'
  }

  if (status === 'update') {
    return 'update'
  }

  if (status === 'error') {
    return 'error'
  }

  return 'success'
}

export default App
