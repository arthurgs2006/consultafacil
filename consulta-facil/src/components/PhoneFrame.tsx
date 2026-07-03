import type { ReactNode } from 'react'

export function PhoneFrame({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <main className={`app-screen ${className}`}>{children}</main>
}
