import type { ReactNode } from 'react'

export function PhoneFrame({ label, children, className = '', fluid = true }: { label: string; children: ReactNode; className?: string; fluid?: boolean }) {
  return (
    <article className={`phone-preview ${fluid ? 'phone-preview--fluid' : ''}`}>
      <span className="phone-preview__label">{label}</span>
      <div className={`phone ${fluid ? 'phone--fluid' : ''}`}>
        <div className="phone__notch" />
        <div className={`phone__screen ${fluid ? 'phone__screen--fluid' : ''} ${className}`}>{children}</div>
      </div>
    </article>
  )
}
