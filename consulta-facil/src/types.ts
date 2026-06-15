export type Role = 'patient' | 'professional' | 'admin'
export type AppointmentStatus = 'scheduled' | 'completed' | 'cancelled'
export type AvatarColor = 'teal' | 'blue' | 'rose'
export type ProfessionalStatus = 'active' | 'inactive'

export interface Doctor {
  id: number
  name: string
  specialty: string
  registration: string
  status: ProfessionalStatus
  rating: number
  initials: string
  color: AvatarColor
}

export interface Appointment {
  id: number
  doctorId: number
  patient: string
  date: string
  time: string
  status: AppointmentStatus
  cancelledAt?: string
}

export type Availability = Record<number, Record<string, string[]>>
