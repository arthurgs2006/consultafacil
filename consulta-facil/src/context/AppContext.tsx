import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { queryAvailability, type AvailabilityQuery, type AvailabilityResult } from '../services/availability'
import { api, ApiError, type ApiAppointment, type ApiProfessional, type ApiSession, type ApiSlot } from '../services/api'
import type { Appointment, Availability, Doctor, Role } from '../types'

type SlotIds = Record<number, Record<string, Record<string, number>>>
interface NewProfessional { name: string; email: string; password: string; specialty: string; registration: string }

interface AppContextValue {
  session: ApiSession | null
  doctors: Doctor[]
  appointments: Appointment[]
  availability: Availability
  loading: boolean
  toast: string
  notify: (message: string) => void
  login: (email: string, password: string) => Promise<Role>
  register: (input: { name: string; email: string; password: string; role: Exclude<Role, 'admin'>; specialty?: string }) => Promise<Role>
  logout: () => void
  getAvailability: (query: AvailabilityQuery) => AvailabilityResult
  schedule: (doctorId: number, date: string, time: string) => Promise<string | null>
  cancelAppointment: (id: number) => Promise<string | null>
  completeAppointment: (id: number) => Promise<string | null>
  toggleAvailability: (doctorId: number, date: string, time: string) => Promise<string | null>
  addDoctor: (input: NewProfessional) => Promise<string | null>
  deleteDoctor: (id: number) => Promise<string | null>
  refresh: () => Promise<void>
}

const AppContext = createContext<AppContextValue | null>(null)
const colors = ['teal', 'blue', 'rose'] as const
const getError = (error: unknown) => error instanceof ApiError || error instanceof Error ? error.message : 'Erro inesperado ao acessar o servidor.'
const initials = (name: string) => name.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase()
const mapDoctors = (items: ApiProfessional[]): Doctor[] => items.map((item, index) => ({ id: item.id, name: item.nome, specialty: item.especialidade ?? 'Clínica geral', registration: item.registro ?? `Cadastro nº ${item.id}`, status: item.ativo ? 'active' : 'inactive', rating: 5, initials: initials(item.nome), color: colors[index % colors.length] }))
const mapAppointments = (items: ApiAppointment[], doctors: Doctor[]): Appointment[] => items.map((item) => ({ id: item.id, doctorId: doctors.find(({ name }) => name === item.nomeProfissional)?.id ?? 0, patient: item.nomePaciente, date: item.data, time: item.horaInicio.slice(0, 5), status: item.status === 'AGENDADA' ? 'scheduled' : item.status === 'CANCELADA' ? 'cancelled' : 'completed' }))

export function AppProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<ApiSession | null>(() => api.restoreSession())
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [availability, setAvailability] = useState<Availability>({})
  const [slotIds, setSlotIds] = useState<SlotIds>({})
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState('')

  const notify = (message: string) => { setToast(message); window.setTimeout(() => setToast(''), 3500) }

  const applySlots = (slots: ApiSlot[]) => {
    const nextAvailability: Availability = {}
    const nextIds: SlotIds = {}
    for (const slot of slots) {
      const time = slot.horaInicio.slice(0, 5)
      nextAvailability[slot.profissionalId] ??= {}
      nextAvailability[slot.profissionalId][slot.data] ??= []
      nextIds[slot.profissionalId] ??= {}
      nextIds[slot.profissionalId][slot.data] ??= {}
      nextAvailability[slot.profissionalId][slot.data].push(time)
      nextIds[slot.profissionalId][slot.data][time] = slot.id
    }
    setAvailability(nextAvailability); setSlotIds(nextIds)
  }

  const loadData = async (activeSession: ApiSession) => {
    setLoading(true)
    try {
      const professionals = await api.professionals(activeSession.token, activeSession.role === 'admin')
      const mappedDoctors = mapDoctors(professionals)
      setDoctors(mappedDoctors)
      if (activeSession.role === 'patient') {
        const [history, slots] = await Promise.all([api.history(activeSession.token), Promise.all(mappedDoctors.filter(({ status }) => status === 'active').map(({ id }) => api.freeSlots(activeSession.token, id))).then((groups) => groups.flat())])
        setAppointments(mapAppointments(history, mappedDoctors)); applySlots(slots)
      } else if (activeSession.role === 'professional') {
        const [agenda, slots] = await Promise.all([api.agenda(activeSession.token), api.mySlots(activeSession.token)])
        setAppointments(mapAppointments(agenda, mappedDoctors)); applySlots(slots)
      } else {
        const allAppointments = await api.adminAppointments(activeSession.token)
        setAppointments(mapAppointments(allAppointments, mappedDoctors)); setAvailability({}); setSlotIds({})
      }
    } catch (error) {
      notify(getError(error))
      if (error instanceof ApiError && error.status === 401) { api.persistSession(null); setSession(null) }
    } finally { setLoading(false) }
  }

  useEffect(() => { if (session) queueMicrotask(() => void loadData(session)) }, [session]) // eslint-disable-line react-hooks/exhaustive-deps

  const authenticate = (nextSession: ApiSession) => { api.persistSession(nextSession); setSession(nextSession); return nextSession.role }
  const login = async (email: string, password: string) => authenticate(await api.login(email, password))
  const register = async (input: { name: string; email: string; password: string; role: Exclude<Role, 'admin'>; specialty?: string }) => authenticate(await api.register(input))
  const logout = () => { api.persistSession(null); setSession(null); setDoctors([]); setAppointments([]); setAvailability({}); setSlotIds({}) }
  const refresh = async () => { if (session) await loadData(session) }
  const getAvailability = (query: AvailabilityQuery) => queryAvailability(query, availability, appointments, doctors)

  const schedule = async (doctorId: number, date: string, time: string) => {
    if (!session) return 'Faça login para agendar.'
    const slotId = slotIds[doctorId]?.[date]?.[time]
    if (!slotId) return 'Este horário não está mais disponível.'
    try { await api.schedule(session.token, slotId); await loadData(session); return null } catch (error) { return getError(error) }
  }
  const cancelAppointment = async (id: number) => {
    if (!session) return 'Sessão expirada.'
    try { await api.cancel(session.token, id); await loadData(session); return null } catch (error) { return getError(error) }
  }
  const completeAppointment = async (id: number) => {
    if (!session) return 'Sessão expirada.'
    try { await api.complete(session.token, id); await loadData(session); return null } catch (error) { return getError(error) }
  }
  const toggleAvailability = async (doctorId: number, date: string, time: string) => {
    if (!session) return 'Sessão expirada.'
    try { const slotId = slotIds[doctorId]?.[date]?.[time]; if (slotId) await api.deleteSlot(session.token, slotId); else await api.createSlot(session.token, date, time); await loadData(session); return null } catch (error) { return getError(error) }
  }
  const addDoctor = async (input: NewProfessional) => {
    if (!session) return 'Sessão expirada.'
    try { await api.addProfessional(session.token, input); await loadData(session); return null } catch (error) { return getError(error) }
  }
  const deleteDoctor = async (id: number) => {
    if (!session) return 'Sessão expirada.'
    try { await api.deleteProfessional(session.token, id); await loadData(session); return null } catch (error) { return getError(error) }
  }

  return <AppContext.Provider value={{ session, doctors, appointments, availability, loading, toast, notify, login, register, logout, getAvailability, schedule, cancelAppointment, completeAppointment, toggleAvailability, addDoctor, deleteDoctor, refresh }}>{children}</AppContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useApp() { const context = useContext(AppContext); if (!context) throw new Error('useApp deve ser usado dentro de AppProvider'); return context }
