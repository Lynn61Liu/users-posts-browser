import { HashIcon } from '../ui/Icons'
import type { UserPost } from '../../usersApi'

type PostGridProps = {
  posts: UserPost[]
}

export function PostGrid({ posts }: PostGridProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {posts.map((post) => (
        <article
          key={post.id}
          className="group rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition-[transform,box-shadow,background-color,border-color] duration-200 hover:-translate-y-0.5 hover:border-violet-200 hover:bg-violet-50/60 hover:shadow-md"
        >
          <div className="mb-4 flex items-center justify-between gap-3">
            <span className="inline-flex items-center gap-1 rounded-full bg-violet-50 px-2.5 py-1 text-xs font-semibold text-violet-600">
              <span className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                Post
              </span>
              <HashIcon className="h-3.5 w-3.5" />
              {post.externalId}
            </span>
          </div>

          <h4 className="text-[15px] font-semibold leading-6 text-slate-900">
            {post.title}
          </h4>
          <p className="mt-3 text-sm leading-6 text-slate-600">{post.body}</p>
        </article>
      ))}
    </div>
  )
}
