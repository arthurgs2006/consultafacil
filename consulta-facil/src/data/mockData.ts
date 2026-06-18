import type { Appointment, Availability, Doctor } from '../types'

export const ALL_SLOTS = ['08:00', '09:00', '10:00', '11:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00']
export const DEMO_NOW = new Date('2026-07-04T09:00:00')

export const initialDoctors: Doctor[] = [
  { id: 1, name: 'Dra. Ana Souza', specialty: 'Cardiologia', registration: 'CRM-SP 184521', status: 'active', rating: 4.9, initials: 'AS', color: 'teal' },
  { id: 2, name: 'Dr. Carlos Lima', specialty: 'Neurologia', registration: 'CRM-SP 207842', status: 'active', rating: 4.8, initials: 'CL', color: 'blue' },
  { id: 3, name: 'Dra. Beatriz Oliveira', specialty: 'Odontologia', registration: 'CRO-SP 98214', status: 'active', rating: 4.7, initials: 'BO', color: 'rose' },
  { id: 4, name: 'Dra. Juliana Martins', specialty: 'Psicologia', registration: 'CRP 06/148721', status: 'active', rating: 4.9, initials: 'JM', color: 'teal' },
  { id: 5, name: 'Dr. Rafael Costa', specialty: 'Fisioterapia', registration: 'CREFITO-3 312845-F', status: 'active', rating: 4.6, initials: 'RC', color: 'blue' },
  { id: 6, name: 'Dra. Marina Alves', specialty: 'Ortopedia', registration: 'CRM-SP 196430', status: 'inactive', rating: 4.5, initials: 'MA', color: 'rose' },
]

export const initialAppointments: Appointment[] = [
  { id: 1, doctorId: 1, patient: 'Maria Silva', date: '2026-06-10', time: '09:00', status: 'completed' },
  { id: 2, doctorId: 2, patient: 'Maria Silva', date: '2026-06-12', time: '14:00', status: 'completed' },
  { id: 3, doctorId: 1, patient: 'Maria Silva', date: '2026-07-06', time: '09:00', status: 'scheduled' },
  { id: 4, doctorId: 4, patient: 'João Pereira', date: '2026-07-06', time: '14:00', status: 'scheduled' },
  { id: 5, doctorId: 3, patient: 'Lucas Mendes', date: '2026-07-07', time: '10:00', status: 'scheduled' },
]

export const initialAvailability: Availability = Object.fromEntries(
  initialDoctors.map(({ id }) => [id, {}]),
)

for (const date of ['2026-07-06', '2026-07-07', '2026-07-08', '2026-07-09', '2026-07-10']) {
  initialAvailability[1][date] = ['09:00', '10:00', '11:00', '14:00', '15:00', '16:00']
  initialAvailability[2][date] = ['08:00', '09:00', '10:00', '14:00', '15:00']
  initialAvailability[3][date] = ['09:00', '10:00', '11:00', '13:00', '14:00']
  initialAvailability[4][date] = ['08:00', '09:00', '14:00', '15:00', '16:00', '17:00']
  initialAvailability[5][date] = ['08:00', '10:00', '11:00', '14:00', '16:00']
}
