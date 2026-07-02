import { describe, expect, it } from 'vitest'
import type { Appointment } from '../types'
import { validateCancellation, validateScheduling } from './appointmentRules'

const now = new Date('2026-07-04T09:00:00')
const scheduled: Appointment = {
  id: 1,
  doctorId: 1,
  patient: 'Maria Silva',
  date: '2026-07-06',
  time: '09:00',
  status: 'scheduled',
}

describe('regras de agendamento', () => {
  it('impede conflito para o mesmo profissional, data e horário', () => {
    expect(validateScheduling(scheduled, [scheduled], now)).toBe('Este horário já está ocupado.')
  })

  it('exige duas horas de antecedência', () => {
    const candidate = { doctorId: 1, date: '2026-07-04', time: '10:00' }
    expect(validateScheduling(candidate, [], now)).toBe('Agendamentos exigem 2h de antecedência.')
  })

  it('permite horário sem conflito e com antecedência', () => {
    expect(validateScheduling(scheduled, [], now)).toBeNull()
  })
})

describe('regras de cancelamento', () => {
  it('exige 24 horas de antecedência', () => {
    const appointment = { ...scheduled, date: '2026-07-05', time: '08:00' }
    expect(validateCancellation(appointment, [appointment], now)).toBe('Cancelamentos exigem 24h de antecedência.')
  })

  it('impede o quarto cancelamento do paciente no mês', () => {
    const previous = [2, 3, 4].map((id): Appointment => ({
      ...scheduled,
      id,
      status: 'cancelled',
      cancelledAt: `2026-07-0${id}T12:00:00.000Z`,
    }))
    expect(validateCancellation(scheduled, [scheduled, ...previous], now)).toBe('Limite de 3 cancelamentos mensais atingido.')
  })

  it('permite cancelamento dentro do limite e com antecedência', () => {
    expect(validateCancellation(scheduled, [scheduled], now)).toBeNull()
  })
})
