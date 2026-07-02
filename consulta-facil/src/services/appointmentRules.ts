import type { Appointment } from '../types'

const HOUR_MS = 3_600_000

export const appointmentDateTime = ({ date, time }: Pick<Appointment, 'date' | 'time'>) => new Date(`${date}T${time}:00`)

export function validateScheduling(
  candidate: Pick<Appointment, 'doctorId' | 'date' | 'time'>,
  appointments: Appointment[],
  now: Date,
  minimumNoticeHours = 2,
): string | null {
  if (appointmentDateTime(candidate).getTime() - now.getTime() < minimumNoticeHours * HOUR_MS) {
    return `Agendamentos exigem ${minimumNoticeHours}h de antecedência.`
  }

  const hasConflict = appointments.some((item) =>
    item.doctorId === candidate.doctorId
    && item.date === candidate.date
    && item.time === candidate.time
    && item.status !== 'cancelled',
  )
  return hasConflict ? 'Este horário já está ocupado.' : null
}

export function validateCancellation(
  appointment: Appointment | undefined,
  appointments: Appointment[],
  now: Date,
  minimumNoticeHours = 24,
  monthlyLimit = 3,
): string | null {
  if (!appointment) return 'Consulta não encontrada.'
  if (appointment.status !== 'scheduled') return 'Apenas consultas agendadas podem ser canceladas.'
  if (appointmentDateTime(appointment).getTime() - now.getTime() < minimumNoticeHours * HOUR_MS) {
    return `Cancelamentos exigem ${minimumNoticeHours}h de antecedência.`
  }

  const currentMonth = now.toISOString().slice(0, 7)
  const monthlyCancellations = appointments.filter((item) =>
    item.patient === appointment.patient
    && item.status === 'cancelled'
    && item.cancelledAt?.slice(0, 7) === currentMonth,
  ).length

  return monthlyCancellations >= monthlyLimit
    ? `Limite de ${monthlyLimit} cancelamentos mensais atingido.`
    : null
}
