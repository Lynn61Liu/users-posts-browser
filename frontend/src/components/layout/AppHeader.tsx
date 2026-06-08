import { RefreshIcon, UsersIcon } from '../ui/Icons'
import { Button } from '../ui/Button'
import { cn } from '../../lib/cn'

export type SyncStatusTone =
  | 'ready'
  | 'syncing'
  | 'success'
  | 'no_change'
  | 'update'
  | 'error'

type AppHeaderProps = {
  syncStatusLabel: string
  syncDescription: string
  statusKind: SyncStatusTone
  onSync: () => void
  isSyncing?: boolean
}

const statusToneClasses: Record<SyncStatusTone, string> = {
  ready: 'border-slate-200 bg-slate-50 text-slate-700',
  syncing: 'border-blue-200 bg-blue-50 text-blue-700',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  no_change: 'border-sky-200 bg-sky-50 text-sky-700',
  update: 'border-amber-200 bg-amber-50 text-amber-700',
  error: 'border-rose-200 bg-rose-50 text-rose-700',
}

export function AppHeader({
  syncStatusLabel,
  syncDescription,
  statusKind,
  onSync,
  isSyncing = false,
}: AppHeaderProps) {
  return (
    <header className="mx-auto flex w-full max-w-[1480px] flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div className="flex items-center gap-4">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-600 ring-1 ring-blue-100">
          <UsersIcon className="h-6 w-6" />
        </div>
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">
            Users & Posts
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Browse users and their posts imported from JSONPlaceholder.
          </p>
        </div>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <div
          className={cn(
            'flex max-w-[780px] flex-wrap items-center gap-2 rounded-2xl border px-4 py-3 text-sm shadow-sm',
            statusToneClasses[statusKind],
          )}
        >
          <span className="h-2.5 w-2.5 rounded-full bg-current" />
          <span className="font-medium">{syncStatusLabel}</span>
          <span className="text-current/75">{syncDescription}</span>
        </div>
        <Button variant="primary" onClick={onSync} disabled={isSyncing}>
          <RefreshIcon className={cn('h-4 w-4', isSyncing && 'animate-spin')} />
          {isSyncing ? 'Syncing' : 'Sync Data'}
        </Button>
      </div>
    </header>
  )
}
