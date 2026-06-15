import type { AvatarColor } from '../types'

export function Avatar({ initials, color = 'teal', size = 'medium' }: { initials: string; color?: AvatarColor; size?: 'small' | 'medium' | 'large' }) {
  return <span className={`avatar avatar--${color} avatar--${size}`}>{initials}</span>
}
