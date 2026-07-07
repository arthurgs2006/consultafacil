import { Bell, BellRing, Bone, Brain, Clock, HeartPulse, Home, Search, Smile, User } from 'lucide-react'
import { useRef, useState } from 'react'
import { Avatar } from '../../components/Avatar'
import { BottomNav } from '../../components/BottomNav'
import { useApp } from '../../context/AppContext'
import { DoctorCard } from './DoctorCard'
import { HistoryPanel } from './HistoryPanel'
import { ScheduleModal } from './ScheduleModal'

const specialties = [
  { label: 'Cardiologia', icon: HeartPulse, tone: 'red' },
  { label: 'Neurologia', icon: Brain, tone: 'purple' },
  { label: 'Psicologia', icon: Smile, tone: 'blue' },
  { label: 'Ortopedia', icon: Bone, tone: 'orange' },
]

const normalize = (value: string) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()

export function PatientPhone() {
  const { doctors, appointments, session, loading } = useApp()
  const [query, setQuery] = useState('')
  const [specialty, setSpecialty] = useState('')
  const [doctorId, setDoctorId] = useState<number | null>(null)
  const [historyOpen, setHistoryOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const searchInputRef = useRef<HTMLInputElement | null>(null)

  const activeDoctors = doctors.filter(({ status }) => status === 'active')
  const specialtyOptions = [...new Set(activeDoctors.map((doctor) => doctor.specialty))].sort()
  const normalizedQuery = normalize(query.trim())
  const visibleDoctors = activeDoctors.filter((doctor) => {
    const matchesQuery = !normalizedQuery || normalize(`${doctor.name} ${doctor.specialty}`).includes(normalizedQuery)
    return matchesQuery && (!specialty || doctor.specialty === specialty)
  })
  const nextAppointment = appointments.filter(({ status }) => status === 'scheduled').sort((a, b) => `${a.date}${a.time}`.localeCompare(`${b.date}${b.time}`))[0]
  const nextDoctor = doctors.find(({ id }) => id === nextAppointment?.doctorId)

  return (
    <main className="app-screen">
      <header className="patient-header">
        <div><Avatar initials={(session?.name ?? 'Paciente').split(' ').map((part) => part[0]).slice(0, 2).join('')} size="small" /><span><small>Olá, paciente</small><strong>{session?.name}</strong></span></div>
        <button className="icon-button notification" aria-label="Notificações" onClick={() => setNotificationsOpen(true)}><Bell /></button>
      </header>
      <div className="patient-content">
        {nextAppointment && <aside className="reminder"><BellRing /><span><strong>Próxima consulta</strong><small>{nextDoctor?.name} em {nextAppointment.date.split('-').reverse().join('/')} às {nextAppointment.time} — {nextDoctor?.specialty}</small></span></aside>}
        <section className="gradient-hero promo"><small>Sua saúde em primeiro lugar</small><h2>Agende sua consulta<br />com facilidade</h2><button onClick={() => setDoctorId(activeDoctors[0]?.id ?? null)}>Agendar agora →</button></section>
        <section>
          <h2 className="section-title">Especialidades</h2>
          <div className="specialties">{specialties.map(({ label, icon: Icon, tone }) => <button className={specialty === label ? 'is-selected' : ''} onClick={() => setSpecialty((current) => current === label ? '' : label)} key={label}><span className={`specialty specialty--${tone}`}><Icon /></span><small>{label}</small></button>)}</div>
        </section>
        <section className="doctor-list">
          <div className="section-heading"><h2 className="section-title">Profissionais disponíveis</h2><span>{visibleDoctors.length} encontrados</span></div>
          <div className="professional-filters">
            <label className="search-input"><Search /><input ref={searchInputRef} value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por nome ou especialidade" aria-label="Buscar profissional" /></label>
            <select value={specialty} onChange={(event) => setSpecialty(event.target.value)} aria-label="Filtrar por especialidade"><option value="">Todas as especialidades</option>{specialtyOptions.map((item) => <option key={item}>{item}</option>)}</select>
          </div>
          {loading ? <p className="empty-message doctor-list__empty">Carregando profissionais...</p> : visibleDoctors.length ? visibleDoctors.map((doctor) => <DoctorCard doctor={doctor} onSchedule={() => setDoctorId(doctor.id)} key={doctor.id} />) : <p className="empty-message doctor-list__empty">Nenhum profissional encontrado com esses filtros.</p>}
        </section>
      </div>
      <BottomNav items={[
        { label: 'Início', icon: Home },
        { label: 'Histórico', icon: Clock, onClick: () => setHistoryOpen(true) },
        { label: 'Buscar', icon: Search, onClick: () => searchInputRef.current?.focus() },
        { label: 'Perfil', icon: User, onClick: () => setProfileOpen(true) },
      ]} />
      {doctorId !== null && <ScheduleModal initialDoctorId={doctorId} onClose={() => setDoctorId(null)} />}
      {historyOpen && <HistoryPanel onClose={() => setHistoryOpen(false)} />}
      {notificationsOpen && (
        <div className="overlay overlay--center">
          <section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="notifications-title">
            <h3 id="notifications-title">Notificações</h3>
            <p>Você não tem novas notificações no momento.</p>
            <button className="button button--primary" type="button" onClick={() => setNotificationsOpen(false)}>Fechar</button>
          </section>
        </div>
      )}
      {profileOpen && (
        <div className="overlay overlay--center">
          <section className="confirm-dialog profile-dialog" role="dialog" aria-modal="true" aria-labelledby="profile-title">
            <Avatar initials={(session?.name ?? 'Paciente').split(' ').map((part) => part[0]).slice(0, 2).join('')} size="large" />
            <h3 id="profile-title">{session?.name}</h3>
            <span className="profile-role">Paciente</span>
            <div className="profile-stats">
              <div><strong>{appointments.filter(({ status }) => status === 'scheduled').length}</strong><small>Agendadas</small></div>
              <div><strong>{appointments.filter(({ status }) => status === 'completed').length}</strong><small>Concluídas</small></div>
            </div>
            <button className="button button--primary button--wide" type="button" onClick={() => setProfileOpen(false)}>Fechar</button>
          </section>
        </div>
      )}
    </main>
  )
}
