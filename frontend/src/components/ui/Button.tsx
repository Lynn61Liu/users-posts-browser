import { cn } from '../../lib/cn'

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'ghost' | 'icon'
}

const variants: Record<NonNullable<ButtonProps['variant']>, string> = {
  primary:
    'bg-blue-600 text-white shadow-lg shadow-blue-200 hover:bg-blue-500 disabled:bg-blue-300',
  secondary:
    'border border-slate-200 bg-white text-slate-700 shadow-sm hover:border-slate-300 hover:bg-slate-50',
  ghost:
    'text-slate-600 hover:bg-slate-100 hover:text-slate-900 disabled:text-slate-400',
  icon:
    'border border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50',
}

export function Button({
  className,
  variant = 'secondary',
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition focus:outline-none focus:ring-4 focus:ring-blue-100 disabled:cursor-not-allowed',
        variants[variant],
        className,
      )}
      {...props}
    />
  )
}
