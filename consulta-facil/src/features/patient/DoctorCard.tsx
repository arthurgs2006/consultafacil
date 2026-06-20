import { BadgeCheck, Star } from 'lucide-react'
import { Avatar } from '../../components/Avatar'
import type { Doctor } from '../../types'

export function DoctorCard({ doctor, onSchedule }: { doctor: Doctor; onSchedule: () => void }) {
  return (
    <article className="doctor-card">
      <Avatar initials={doctor.initials} color={doctor.color} size="large" />
      <div className="doctor-card__info">
        <strong>{doctor.name}</strong>
        <span>{doctor.specialty}</span>
        <small className="doctor-card__registration"><BadgeCheck />{doctor.registration}</small>
        <small><Star />{doctor.rating.toFixed(1)} · Disponível para agendamento</small>
      </div>
      <button className="button button--primary" onClick={onSchedule}>Agendar</button>
    </article>
  )
}
