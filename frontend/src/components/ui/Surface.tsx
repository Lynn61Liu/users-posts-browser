import { cn } from '../../lib/cn'

type SurfaceProps = React.HTMLAttributes<HTMLDivElement> & {
  padded?: boolean
}

export function Surface({
  className,
  padded = true,
  ...props
}: SurfaceProps) {
  return (
    <div
      className={cn(
        'rounded-[28px] border border-slate-200/80 bg-white/85 shadow-[0_20px_60px_rgba(15,23,42,0.08)] backdrop-blur-xl',
        padded && 'p-6',
        className,
      )}
      {...props}
    />
  )
}
