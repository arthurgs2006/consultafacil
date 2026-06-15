import type { AppointmentStatus } from '../types'

const labels: Record<AppointmentStatus, string> = { scheduled: 'Agendada', completed: 'Concluída', cancelled: 'Cancelada' }

export function StatusBadge({ status }: { status: AppointmentStatus }) {
  return <span className={`status status--${status}`}>{labels[status]}</span>
}
