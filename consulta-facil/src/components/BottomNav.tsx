import type { LucideIcon } from 'lucide-react'

export interface NavItem { label: string; icon: LucideIcon; onClick?: () => void }

export function BottomNav({ items, activeLabel }: { items: NavItem[]; activeLabel?: string }) {
  return (
    <nav className="bottom-nav">
      {items.map(({ label, icon: Icon, onClick }) => (
        <button className={label === activeLabel ? 'is-active' : ''} onClick={onClick} key={label} type="button">
          <Icon />
          <span>{label}</span>
        </button>
      ))}
    </nav>
  )
}
