import type { Appointment, Availability, Doctor } from '../types'

export interface AvailabilityQuery {
  professionalId: number
  date: string
  time?: string
}

export interface AvailabilityResult {
  slots: string[]
  available: boolean
}

/** Consulta os horários livres de um profissional e, opcionalmente, um horário específico. */
export function queryAvailability(
  query: AvailabilityQuery,
  availability: Availability,
  appointments: Appointment[],
  professionals: Doctor[],
): AvailabilityResult {
  const professional = professionals.find(({ id }) => id === query.professionalId)
  if (!professional || professional.status !== 'active' || !query.date) return { slots: [], available: false }

  const occupied = new Set(
    appointments
      .filter(({ doctorId, date, status }) => doctorId === query.professionalId && date === query.date && status !== 'cancelled')
      .map(({ time }) => time),
  )
  const slots = (availability[query.professionalId]?.[query.date] ?? []).filter((slot) => !occupied.has(slot))

  return {
    slots,
    available: query.time ? slots.includes(query.time) : slots.length > 0,
  }
}
