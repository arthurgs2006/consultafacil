import type { Role } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? '/api'
const TOKEN_KEY = 'consulta-facil:token'
const SESSION_KEY = 'consulta-facil:session'

export interface ApiSession { token: string; name: string; role: Role }
export interface ApiProfessional { id: number; nome: string; especialidade: string | null; registro: string | null; ativo: boolean }
export interface ApiSlot { id: number; profissionalId: number; nomeProfissional: string; data: string; horaInicio: string; horaFim: string; reservado: boolean }
export interface ApiAppointment { id: number; nomePaciente: string; nomeProfissional: string; especialidadeProfissional: string; data: string; horaInicio: string; horaFim: string; status: 'AGENDADA' | 'CANCELADA' | 'CONCLUIDA'; criadoEm: string }

const roleMap: Record<string, Role> = { PACIENTE: 'patient', PROFISSIONAL: 'professional', ADMIN: 'admin' }

export class ApiError extends Error {
  status: number
  constructor(message: string, status: number) { super(message); this.status = status }
}

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({})) as { error?: string; errors?: Record<string, string> }
    const validation = body.errors ? Object.values(body.errors)[0] : undefined
    throw new ApiError(body.error ?? validation ?? 'Não foi possível concluir a operação.', response.status)
  }
  return response.status === 204 ? undefined as T : response.json() as Promise<T>
}

function toSession(response: { token: string; nomeUsuario: string; papel: string }): ApiSession {
  return { token: response.token, name: response.nomeUsuario, role: roleMap[response.papel] }
}

export const api = {
  restoreSession(): ApiSession | null {
    try { return JSON.parse(localStorage.getItem(SESSION_KEY) ?? 'null') as ApiSession | null } catch { return null }
  },
  persistSession(session: ApiSession | null) {
    if (session) { localStorage.setItem(TOKEN_KEY, session.token); localStorage.setItem(SESSION_KEY, JSON.stringify(session)) }
    else { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(SESSION_KEY) }
  },
  async login(email: string, senha: string) {
    return toSession(await request('/autenticacao/login', { method: 'POST', body: JSON.stringify({ email, senha }) }))
  },
  async register(input: { name: string; email: string; password: string; role: Exclude<Role, 'admin'>; specialty?: string }) {
    return toSession(await request('/autenticacao/registrar', { method: 'POST', body: JSON.stringify({ nomeUsuario: input.name, email: input.email, senha: input.password, papel: input.role === 'patient' ? 'PACIENTE' : 'PROFISSIONAL', especialidade: input.role === 'professional' ? input.specialty : null }) }))
  },
  professionals: (token: string, admin = false) => request<ApiProfessional[]>(admin ? '/admin/profissionais' : '/profissionais', {}, token),
  freeSlots: (token: string, id: number) => request<ApiSlot[]>(`/horarios/profissional/${id}`, {}, token),
  mySlots: (token: string) => request<ApiSlot[]>('/horarios/meus', {}, token),
  createSlot: (token: string, data: string, time: string) => request<ApiSlot>('/horarios', { method: 'POST', body: JSON.stringify({ data, horaInicio: `${time}:00`, horaFim: `${String(Number(time.slice(0, 2)) + 1).padStart(2, '0')}:00:00` }) }, token),
  deleteSlot: (token: string, id: number) => request<void>(`/horarios/${id}`, { method: 'DELETE' }, token),
  history: (token: string) => request<ApiAppointment[]>('/consultas/historico', {}, token),
  agenda: (token: string) => request<ApiAppointment[]>('/consultas/agenda', {}, token),
  adminAppointments: (token: string) => request<ApiAppointment[]>('/admin/consultas', {}, token),
  schedule: (token: string, horarioId: number) => request<ApiAppointment>('/consultas', { method: 'POST', body: JSON.stringify({ horarioId }) }, token),
  cancel: (token: string, id: number) => request<void>(`/consultas/${id}`, { method: 'DELETE' }, token),
  complete: (token: string, id: number) => request<ApiAppointment>(`/consultas/${id}/concluir`, { method: 'PUT' }, token),
  addProfessional: (token: string, input: { name: string; email: string; password: string; specialty: string; registration: string }) => request<ApiProfessional>('/admin/profissionais', { method: 'POST', body: JSON.stringify({ nome: input.name, email: input.email, senha: input.password, especialidade: input.specialty, registro: input.registration }) }, token),
  deleteProfessional: (token: string, id: number) => request<void>(`/admin/profissionais/${id}`, { method: 'DELETE' }, token),
}
