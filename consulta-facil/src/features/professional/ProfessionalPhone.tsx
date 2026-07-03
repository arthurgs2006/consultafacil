import { BarChart2, CalendarDays, Search, Settings, Users } from 'lucide-react'
import { useState } from 'react'
import { Avatar } from '../../components/Avatar'
import { BottomNav } from '../../components/BottomNav'
import { StatusBadge } from '../../components/StatusBadge'
import { useApp } from '../../context/AppContext'
import { ALL_SLOTS } from '../../data/mockData'
import type { AppointmentStatus } from '../../types'
import { formatDate } from '../../utils/date'
import { Calendar } from './Calendar'

export function ProfessionalPhone() {
  const { session, doctors, appointments, availability, toggleAvailability, completeAppointment, notify } = useApp()
  const today = new Date()
  const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1)
  const initialDate = `${tomorrow.getFullYear()}-${String(tomorrow.getMonth() + 1).padStart(2, '0')}-${String(tomorrow.getDate()).padStart(2, '0')}`
  const [viewDate, setViewDate] = useState(new Date(tomorrow.getFullYear(), tomorrow.getMonth(), 1))
  const [selectedDate, setSelectedDate] = useState(initialDate)
  const [dateFilter, setDateFilter] = useState(initialDate)
  const [patientFilter, setPatientFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState<AppointmentStatus | ''>('')
  const [activeTab, setActiveTab] = useState<'agenda' | 'patients' | 'reports' | 'config'>('agenda')
  const professional = doctors.find(({ name }) => name === session?.name)
  const professionalId = professional?.id ?? 0
  const slots = availability[professionalId]?.[selectedDate] ?? []
  const normalizedPatient = patientFilter.trim().toLowerCase()
  const items = appointments.filter((item) => (!professionalId || item.doctorId === professionalId)
    && (!dateFilter || item.date === dateFilter)
    && (!normalizedPatient || item.patient.toLowerCase().includes(normalizedPatient))
    && (!statusFilter || item.status === statusFilter))

  const selectDay = (date: string) => { setSelectedDate(date); setDateFilter(date) }

  const agendaPane = (
    <>
      <h2 className="section-title">Gerenciar horários disponíveis</h2>
      <Calendar viewDate={viewDate} selected={selectedDate} onViewChange={setViewDate} onSelect={selectDay} />
      <section className="card availability">
        <header><strong>Horários em <em>{formatDate(selectedDate)}</em></strong><small>Toque para ativar/desativar</small></header>
        <div className="slot-grid">
          {ALL_SLOTS.map((slot) => {
            const enabled = slots.includes(slot)
            return (
              <button
                type="button"
                disabled={!professionalId}
                className={`slot-toggle ${enabled ? 'is-on' : ''}`}
                aria-pressed={enabled}
                aria-label={`${slot}: ${enabled ? 'disponível' : 'bloqueado'}`}
                onClick={() => void (async () => {
                  const error = await toggleAvailability(professionalId, selectedDate, slot)
                  notify(error ?? `Horário ${slot} ${enabled ? 'bloqueado' : 'disponibilizado'}.`)
                })()}
                key={slot}
              >
                {slot}
              </button>
            )
          })}
        </div>
        <footer><span><i className="available" />Disponível</span><span><i />Bloqueado</span></footer>
      </section>
      <section className="professional-appointments">
        <div className="section-heading">
          <h2 className="section-title">Agenda de consultas</h2>
          <button className="text-link" type="button" onClick={() => { setDateFilter(''); setPatientFilter(''); setStatusFilter('') }}>Limpar filtros</button>
        </div>
        <div className="filters professional-filters">
          <input type="date" value={dateFilter} onChange={(event) => setDateFilter(event.target.value)} aria-label="Filtrar consultas por data" />
          <label className="search-input"><Search /><input value={patientFilter} onChange={(event) => setPatientFilter(event.target.value)} placeholder="Paciente..." aria-label="Filtrar por paciente" /></label>
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as AppointmentStatus | '')} aria-label="Filtrar por status">
            <option value="">Todos os status</option>
            <option value="scheduled">Agendadas</option>
            <option value="completed">Concluídas</option>
            <option value="cancelled">Canceladas</option>
          </select>
        </div>
        {items.length === 0 ? <p className="empty-message">Nenhuma consulta encontrada.</p> : items.map((item) => (
          <article className="professional-appointment" key={item.id}>
            <div><strong>{item.patient}</strong><small>{formatDate(item.date)} às {item.time}</small></div>
            <StatusBadge status={item.status} />
            {item.status === 'scheduled' && <button type="button" onClick={() => void (async () => { const error = await completeAppointment(item.id); notify(error ?? 'Consulta marcada como concluída.') })()}>Concluir</button>}
          </article>
        ))}
      </section>
    </>
  )

  const patientsPane = (
    <section className="professional-appointments">
      <div className="section-heading"><h2 className="section-title">Pacientes</h2><span>{items.filter((item) => item.doctorId === professionalId).length} consulta(s)</span></div>
      {items.filter((item) => item.doctorId === professionalId).length === 0 ? <p className="empty-message">Nenhum paciente encontrado.</p> : items.filter((item) => item.doctorId === professionalId).map((item) => (
        <article className="professional-appointment" key={`${item.patient}-${item.id}`}>
          <div><strong>{item.patient}</strong><small>{formatDate(item.date)} às {item.time}</small></div>
          <StatusBadge status={item.status} />
        </article>
      ))}
    </section>
  )

  const reportsPane = (
    <section className="professional-appointments">
      <div className="section-heading"><h2 className="section-title">Relatórios</h2></div>
      <div className="stats" aria-label="Relatórios de atendimento"><div><strong>{items.length}</strong><small>Total</small></div><div><strong>{items.filter((item) => item.status === 'scheduled').length}</strong><small>Agendadas</small></div><div><strong>{items.filter((item) => item.status === 'completed').length}</strong><small>Concluídas</small></div></div>
      <p className="empty-message">Use este relatório para monitorar sua agenda.</p>
    </section>
  )

  const configPane = (
    <section className="professional-appointments"><div className="section-heading"><h2 className="section-title">Configuração</h2></div><p>Configurações ainda não disponíveis nesta versão.</p><button className="button button--primary" type="button" onClick={() => notify('Configurações ainda não estão disponíveis nesta versão.')}>Verificar opções</button></section>
  )

  return (
    <main className="app-screen">
      <header className="professional-header"><div><Avatar initials={professional?.initials ?? 'PR'} color="blue" /><span><small>Profissional</small><strong>{session?.name}</strong></span></div><em>{professional?.specialty}</em></header>
      <main className="professional-content">
        {activeTab === 'agenda' && agendaPane}
        {activeTab === 'patients' && patientsPane}
        {activeTab === 'reports' && reportsPane}
        {activeTab === 'config' && configPane}
      </main>
      <BottomNav items={[{ label: 'Agenda', icon: CalendarDays, onClick: () => setActiveTab('agenda') }, { label: 'Pacientes', icon: Users, onClick: () => setActiveTab('patients') }, { label: 'Relatórios', icon: BarChart2, onClick: () => setActiveTab('reports') }, { label: 'Config.', icon: Settings, onClick: () => setActiveTab('config') }]} activeLabel={activeTab === 'agenda' ? 'Agenda' : activeTab === 'patients' ? 'Pacientes' : activeTab === 'reports' ? 'Relatórios' : 'Config.'} />
    </main>
  )
}
