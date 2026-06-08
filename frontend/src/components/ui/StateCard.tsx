import { cn } from '../../lib/cn'

type StateCardProps = {
  title: string
  description: string
  variant?: 'default' | 'empty' | 'error'
  className?: string
}

export function StateCard({
  title,
  description,
  variant = 'default',
  className,
}: StateCardProps) {
  return (
    <div
      className={cn(
        'w-full rounded-2xl border p-5',
        variant === 'error' && 'border-rose-200 bg-rose-50/80',
        variant === 'empty' && 'border-emerald-200 bg-emerald-50/70',
        variant === 'default' && 'border-slate-200 bg-slate-50/80',
        className,
      )}
    >
      <p className="text-[15px] font-semibold text-slate-900">{title}</p>
      <p className="mt-2 text-sm leading-6 text-slate-600">{description}</p>
    </div>
  )
}
