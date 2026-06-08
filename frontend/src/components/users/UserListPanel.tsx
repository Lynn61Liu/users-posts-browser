import { ChevronRightIcon } from '../ui/Icons'
import { Button } from '../ui/Button'
import { StateCard } from '../ui/StateCard'
import { Surface } from '../ui/Surface'
import { Avatar } from '../ui/Avatar'
import { cn } from '../../lib/cn'
import type { UserSummary } from '../../usersApi'

type UserListPanelProps = {
  users: UserSummary[]
  visibleUsers: UserSummary[]
  selectedUserId: number | null
  onSelectUser: (userId: number) => void
  isLoading: boolean
  isError: boolean
  isEmpty: boolean
  errorTitle?: string
  errorDescription?: string
}

const avatarTones = [
  'bg-indigo-100 text-indigo-700 ring-indigo-200',
  'bg-sky-100 text-sky-700 ring-sky-200',
  'bg-emerald-100 text-emerald-700 ring-emerald-200',
  'bg-amber-100 text-amber-700 ring-amber-200',
  'bg-rose-100 text-rose-700 ring-rose-200',
  'bg-violet-100 text-violet-700 ring-violet-200',
]

export function UserListPanel({
  users,
  visibleUsers,
  selectedUserId,
  onSelectUser,
  isLoading,
  isError,
  isEmpty,
  errorTitle = 'Could not load users.',
  errorDescription = 'Refresh the page or check the backend logs.',
}: UserListPanelProps) {
  const selectedIndex = visibleUsers.findIndex((user) => user.id === selectedUserId)
  const selectedLabel =
    selectedIndex >= 0 ? `${selectedIndex + 1}` : visibleUsers.length ? '1' : '0'

  return (
    <Surface className="flex h-full flex-col gap-5">
      <div>
        <div className="flex items-baseline justify-between gap-3">
          <h2 className="text-xl font-semibold text-slate-900">
            Users <span className="text-slate-500">({users.length})</span>
          </h2>
        </div>

      </div>

      {isLoading ? (
        <StateCard
          title="Loading users..."
          description="Fetching imported users from the backend."
        />
      ) : null}

      {isError ? (
        <StateCard
          variant="error"
          title={errorTitle}
          description={errorDescription}
        />
      ) : null}

      {isEmpty ? (
        <StateCard
          variant="empty"
          title="No users imported yet."
          description="Run sync to populate the database, then come back here."
        />
      ) : null}

      {!isLoading && !isError && !isEmpty && visibleUsers.length > 0 ? (
        <>
          <ul className="space-y-2.5">
            {visibleUsers.map((user, index) => {
              const isSelected = user.id === selectedUserId
              const initials = userInitials(user.name)
              const toneClassName = avatarTones[index % avatarTones.length]

              return (
                <li key={user.id}>
                  <button
                    type="button"
                    onClick={() => onSelectUser(user.id)}
                    className={cn(
                      'group flex w-full items-center gap-4 rounded-2xl border bg-white px-4 py-3 text-left shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-blue-200 hover:shadow-md',
                      isSelected
                        ? 'border-blue-500 bg-gradient-to-r from-blue-100 via-blue-50 to-white shadow-[0_10px_25px_rgba(37,99,235,0.12)] ring-2 ring-blue-300/60'
                        : 'border-slate-200',
                    )}
                    aria-current={isSelected ? 'true' : undefined}
                  >
                    <Avatar initials={initials} toneClassName={toneClassName} />

                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-slate-900">
                        {user.name}
                      </p>
                      <p className="mt-1 text-sm text-slate-500">
                        @{user.username}
                      </p>
                    </div>

                    <div className="ml-auto flex min-w-0 items-center gap-3">
                      <p className="max-w-[220px] truncate text-sm text-slate-500">
                        {user.email}
                      </p>
                      <ChevronRightIcon className="h-4 w-4 flex-none text-slate-400 transition group-hover:text-blue-600" />
                    </div>
                  </button>
                </li>
              )
            })}
          </ul>

          <div className="mt-auto flex items-center justify-between gap-3 border-t border-slate-200 pt-4 text-sm text-slate-500">
            <p>
              Showing 1 to {visibleUsers.length} of{' '}
              {users.length} users
            </p>
            <div className="flex items-center gap-2">
              <Button variant="icon" aria-label="Previous users page" disabled>
                <span className="text-lg leading-none">‹</span>
              </Button>
              <Button variant="primary" className="min-w-10 px-3 py-2">
                1
              </Button>
              <Button variant="icon" aria-label="Next users page" disabled>
                <span className="text-lg leading-none">›</span>
              </Button>
            </div>
          </div>

          <p className="sr-only">{selectedLabel}</p>
        </>
      ) : null}

    </Surface>
  )
}

function userInitials(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}
