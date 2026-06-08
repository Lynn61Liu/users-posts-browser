import {
  BuildingIcon,
  GlobeIcon,
  MapPinIcon,
  PhoneIcon,
} from '../ui/Icons'
import { Button } from '../ui/Button'
import { Avatar } from '../ui/Avatar'
import { StateCard } from '../ui/StateCard'
import { Surface } from '../ui/Surface'
import { cn } from '../../lib/cn'
import type { UserDetail, UserPost } from '../../usersApi'
import { PostGrid } from './PostGrid'

type UserDetailPanelProps = {
  user: UserDetail | null
  posts: UserPost[]
  isLoading: boolean
  isError: boolean
  isEmpty: boolean
  errorTitle?: string
  errorDescription?: string
  postPage: number
  onPrevPostPage: () => void
  onNextPostPage: () => void
  onSelectPostPage: (page: number) => void
}

export function UserDetailPanel({
  user,
  posts,
  isLoading,
  isError,
  isEmpty,
  errorTitle = 'Could not load the selected user.',
  errorDescription = 'Try selecting a different user or refresh the page.',
  postPage,
  onPrevPostPage,
  onNextPostPage,
  onSelectPostPage,
}: UserDetailPanelProps) {
  const postsPerPage = 6
  const totalPages = Math.max(1, Math.ceil(posts.length / postsPerPage))
  const safePage = Math.min(postPage, totalPages)
  const start = (safePage - 1) * postsPerPage
  const postsToDisplay = posts.slice(start, start + postsPerPage)

  return (
    <div className="flex h-full flex-col gap-6">
      <Surface className="relative overflow-hidden">
        <div className="mb-6 flex items-start justify-between gap-4">
          <div className="flex items-center gap-4">
            <Avatar
              initials={user ? userInitials(user.name) : '??'}
              toneClassName="bg-violet-100 text-violet-700 ring-violet-200"
              sizeClassName="h-16 w-16 text-xl"
            />

            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-2xl font-semibold tracking-tight text-slate-900">
                  {user?.name ?? 'User details'}
                </h2>
                {user ? (
                  <span className="rounded-full border border-slate-200 bg-slate-50 px-2.5 py-1 text-sm text-slate-600">
                    @{user.username}
                  </span>
                ) : null}
              </div>

              {user ? (
                <a
                  className="mt-2 inline-block text-base font-medium text-blue-600 hover:text-blue-700"
                  href={`mailto:${user.email}`}
                >
                  {user.email}
                </a>
              ) : null}
            </div>
          </div>

        </div>

        {isLoading ? (
          <StateCard
            title="Loading selected user..."
            description="Fetching full profile details and related posts."
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
            title="Select a user to inspect details."
            description="The selected profile and related posts will appear here."
          />
        ) : null}

        {!isLoading && !isError && !isEmpty && user ? (
          <>
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <div className="space-y-4">
                <InfoTile
                  icon={<PhoneIcon className="h-5 w-5" />}
                  title="Phone"
                  body={<p className="text-base  text-slate-900">{user.phone}</p>}
                />
                <InfoTile
                  icon={<GlobeIcon className="h-5 w-5" />}
                  title="Website"
                  body={
                    <a
                      className="text-base font-semibold text-blue-600 hover:text-blue-700"
                      href={`https://${user.website}`}
                      target="_blank"
                      rel="noreferrer"
                    >
                      {user.website}
                    </a>
                  }
                />
              </div>
              <InfoTile
                icon={<MapPinIcon className="h-5 w-5" />}
                title="Address"
                body={
                  <StructuredFacts
                    items={[
                      {
                        label: 'Street',
                        value: `${user.address.street}, ${user.address.suite}`,
                
                      },
                      {
                        label: 'City',
                        value: `${user.address.city}, ${user.address.zipcode}`,
                      },
                      {
                        label: 'Geo',
                        value: `${user.address.geo.lat}, ${user.address.geo.lng}`,
                        mono: true,
                      },
                    ]}
                  />
                }
              />
              <InfoTile
                icon={<BuildingIcon className="h-5 w-5" />}
                title="Company"
                body={
                  <StructuredFacts
                    items={[
                      {
                        label: 'Name',
                        value: user.company.name,
                
                      },
                      {
                        label: 'CatchPhrase',
                        value: user.company.catchPhrase,
                      },
                      {
                        label: 'Bs',
                        value: user.company.bs,
                      },
                    ]}
                  />
                }
              />
            </div>
          </>
        ) : null}
      </Surface>

      <Surface className="flex min-h-0 flex-1 flex-col bg-gradient-to-br from-violet-50 to-white">
        <div className="mb-5 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h3 className="text-xl font-semibold text-slate-900">
              Posts <span className="text-slate-500">({posts.length})</span>
            </h3>
          </div>
        </div>

        <div className="flex min-h-0 flex-1 flex-col">
          {posts.length === 0 ? (
            <div className="flex flex-1 items-start ">
              <StateCard
                variant="empty"
                title="No related posts."
                description="This user has not any posts yet."
                className="flex  flex-col "
              />
            </div>
          ) : (
            <>
              <PostGrid posts={postsToDisplay} />

              <div className="mt-5 flex items-center justify-center gap-2">
                <Button variant="icon" onClick={onPrevPostPage} disabled={safePage === 1}>
                  <span className="text-lg leading-none">‹</span>
                </Button>
                {Array.from({ length: totalPages }, (_, index) => index + 1).map(
                  (page) => (
                    <Button
                      key={page}
                      variant={page === safePage ? 'primary' : 'secondary'}
                      className={cn('min-w-10 px-3 py-2', page !== safePage && 'bg-white')}
                      onClick={() => onSelectPostPage(page)}
                    >
                      {page}
                    </Button>
                  ),
                )}
                <Button variant="icon" onClick={onNextPostPage} disabled={safePage === totalPages}>
                  <span className="text-lg leading-none">›</span>
                </Button>
              </div>
            </>
          )}
        </div>
      </Surface>
    </div>
  )
}

type InfoTileProps = {
  icon: React.ReactNode
  title: string
  body: React.ReactNode
}

function InfoTile({ icon, title, body }: InfoTileProps) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center gap-2 text-slate-900">
        {icon}
        <h4 className=" font-semibold tracking-tight text-slate-900">
          {title}
        </h4>
      </div>
      <div className="mt-4 space-y-3 text-sm leading-6 text-slate-600">
        {body}
      </div>
    </div>
  )
}

type StructuredFact = {
  label: string
  value: string
  emphasis?: boolean
  mono?: boolean
}

function StructuredFacts({ items }: { items: StructuredFact[] }) {
  return (
    <div >
      {items.map((item) => (
        <div key={item.label} className="space-y-0.6">
          <div className="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400">
            {item.label}
          </div>
          <div
            className={cn(
              'text-sm leading-6 text-slate-700',
              item.emphasis && 'text-base font-semibold leading-7 text-slate-900',
              item.mono && 'font-mono text-[13px] break-all',
            )}
          >
            {item.value}
          </div>
        </div>
      ))}
    </div>
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
