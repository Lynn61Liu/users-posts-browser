import { cn } from '../../lib/cn'

type AvatarProps = {
  initials: string
  toneClassName?: string
  sizeClassName?: string
}

export function Avatar({
  initials,
  toneClassName = 'bg-blue-50 text-blue-700 ring-blue-100',
  sizeClassName = 'h-12 w-12 text-base',
}: AvatarProps) {
  return (
    <div
      className={cn(
        'inline-flex items-center justify-center rounded-full ring-1 font-semibold',
        toneClassName,
        sizeClassName,
      )}
      aria-hidden="true"
    >
      {initials}
    </div>
  )
}
