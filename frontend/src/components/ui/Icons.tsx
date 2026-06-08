type IconProps = {
  className?: string
}

function IconBase({
  className,
  children,
}: IconProps & {
  children: React.ReactNode
}) {
  return (
    <svg
      aria-hidden="true"
      viewBox="0 0 24 24"
      fill="none"
      className={className}
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {children}
    </svg>
  )
}

export function UsersIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="M16 21v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-1a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </IconBase>
  )
}

export function SearchIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </IconBase>
  )
}

export function RefreshIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="M21 12a9 9 0 1 1-2.64-6.36" />
      <path d="M21 3v6h-6" />
    </IconBase>
  )
}

export function ChevronRightIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="m9 18 6-6-6-6" />
    </IconBase>
  )
}

export function ChevronLeftIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="m15 18-6-6 6-6" />
    </IconBase>
  )
}

export function ArrowLeftIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="m12 19-7-7 7-7" />
      <path d="M19 12H5" />
    </IconBase>
  )
}

export function MapPinIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="M12 21s6-4.35 6-10a6 6 0 0 0-12 0c0 5.65 6 10 6 10Z" />
      <circle cx="12" cy="11" r="2" />
    </IconBase>
  )
}

export function PhoneIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.3 19.3 0 0 1-6-6A19.8 19.8 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.12.9.33 1.77.63 2.6a2 2 0 0 1-.45 2.11L8 9.69a16 16 0 0 0 6.31 6.31l1.26-1.26a2 2 0 0 1 2.11-.45c.83.3 1.7.51 2.6.63A2 2 0 0 1 22 16.92Z" />
    </IconBase>
  )
}

export function GlobeIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <circle cx="12" cy="12" r="9" />
      <path d="M3 12h18" />
      <path d="M12 3a15 15 0 0 1 0 18" />
      <path d="M12 3a15 15 0 0 0 0 18" />
    </IconBase>
  )
}

export function BuildingIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="M4 21V7l8-4 8 4v14" />
      <path d="M4 11h16" />
      <path d="M8 21v-5h8v5" />
      <path d="M10 7v2" />
      <path d="M14 7v2" />
    </IconBase>
  )
}

export function HashIcon({ className }: IconProps) {
  return (
    <IconBase className={className}>
      <path d="M5 9h14" />
      <path d="M5 15h14" />
      <path d="M10 3 8 21" />
      <path d="M16 3 14 21" />
    </IconBase>
  )
}
