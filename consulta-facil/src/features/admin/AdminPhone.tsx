import { BarChart2, FileBarChart, LayoutDashboard, Plus, Settings, Shield, UserPlus, Users } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Avatar } from '../../components/Avatar'
import { BottomNav } from '../../components/BottomNav'
import { PhoneFrame } from '../../components/PhoneFrame'
import { StatusBadge } from '../../components/StatusBadge'
import { useApp } from '../../context/AppContext'
import { formatDate } from '../../utils/date'

export function AdminPhone() {
  const { doctors, appointments, addDoctor, deleteDoctor, cancelAppointment, notify } = useApp()
  const [activeTab, setActiveTab] = useState<'dashboard' | 'users' | 'reports' | 'config'>('dashboard')
  const [formOpen, setFormOpen] = useState(false)
  const [name, setName] = useState('')
  const [specialty, setSpecialty] = useState('')
  const [registration, setRegistration] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [dateFilter, setDateFilter] = useState('')
  const [doctorFilter, setDoctorFilter] = useState('')
  const [deleteId, setDeleteId] = useState<number | null>(null)

  const filtered = appointments.filter((item) => (!dateFilter || item.date === dateFilter) && (!doctorFilter || item.doctorId === Number(doctorFilter)))
  const deleteTarget = doctors.find(({ id }) => id === deleteId)

  const submit = async (event: FormEvent) => {
    event.preventDefault()

    const error = await addDoctor({
      name: name.trim(),
      email: email.trim(),
      password,
      specialty: specialty.trim(),
      registration: registration.trim(),
    })

    if (error) {
      notify(error)
      return
    }

    setName('')
    setEmail('')
    setPassword('')
    setSpecialty('')
    setRegistration('')
    setFormOpen(false)
    notify('Profissional cadastrado com sucesso!')
  }

  const confirmDelete = async () => {
    if (deleteId === null) return

    const error = await deleteDoctor(deleteId)
    setDeleteId(null)
    notify(error ?? 'Profissional desativado com sucesso.')
  }

  const dashboardPane = (
    <>
      <section className="stats" aria-label="Indicadores de agendamentos">
        <div><strong>{appointments.length}</strong><small>Total</small></div>
        <div><strong>{appointments.filter(({ status }) => status === 'scheduled').length}</strong><small>Agendadas</small></div>
        <div><strong>{appointments.filter(({ status }) => status === 'cancelled').length}</strong><small>Canceladas</small></div>
      </section>
      <section className="quick-actions">
        <button type="button" onClick={() => setFormOpen(true)}><UserPlus /><strong>Novo profissional</strong><small>Cadastrar na plataforma</small></button>
        <button type="button" onClick={() => notify('Os indicadores são atualizados diretamente pela API.')}><FileBarChart /><strong>Relatório</strong><small>Conferir indicadores</small></button>
      </section>
      <section className="admin-appointments">
        <h2 className="section-title">Todos os agendamentos</h2>
        <div className="filters">
          <input type="date" value={dateFilter} onChange={(event) => setDateFilter(event.target.value)} aria-label="Filtrar agendamentos por data" />
          <select value={doctorFilter} onChange={(event) => setDoctorFilter(event.target.value)} aria-label="Filtrar por profissional">
            <option value="">Todos profissionais</option>
            {doctors.map((doctor) => <option value={doctor.id} key={doctor.id}>{doctor.name}</option>)}
          </select>
        </div>
        {filtered.length === 0 ? (
          <p className="empty-message">Nenhum agendamento encontrado.</p>
        ) : (
          filtered.map((item) => {
            const doctor = doctors.find(({ id }) => id === item.doctorId)
            return (
              <article className="admin-appointment" key={item.id}>
                <div>
                  <strong>{item.patient}</strong>
                  <em>{doctor?.name ?? 'Profissional removido'}</em>
                  <small>{formatDate(item.date)} às {item.time}</small>
                  {item.status === 'scheduled' && (
                    <button type="button" onClick={() => void (async () => {
                      const error = await cancelAppointment(item.id)
                      notify(error ?? 'Consulta cancelada pelo administrador.')
                    })()}>Cancelar</button>
                  )}
                </div>
                <StatusBadge status={item.status} />
              </article>
            )
          })
        )}
      </section>
    </>
  )

  const usersPane = (
    <section className="admin-doctors" aria-label="Profissionais cadastrados">
      <div className="section-heading">
        <h2 className="section-title">Usuários</h2>
        <button className="button button--primary" type="button" onClick={() => setFormOpen((value) => !value)} aria-expanded={formOpen}><Plus />Novo</button>
      </div>
      {formOpen && (
        <form className="card professional-form" onSubmit={(event) => void submit(event)}>
          <h3>Novo profissional</h3>
          <label>Nome completo<input required value={name} onChange={(event) => setName(event.target.value)} /></label>
          <label>E-mail de acesso<input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
          <label>Senha inicial<input required minLength={8} type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          <label>Especialidade<input required value={specialty} onChange={(event) => setSpecialty(event.target.value)} /></label>
          <label>Registro profissional<input required value={registration} onChange={(event) => setRegistration(event.target.value)} placeholder="Ex.: CRM-SP 123456" /></label>
          <div>
            <button className="button button--primary" type="submit">Salvar</button>
            <button type="button" className="button button--muted" onClick={() => setFormOpen(false)}>Cancelar</button>
          </div>
        </form>
      )}
      {doctors.map((doctor) => (
        <article key={doctor.id}>
          <Avatar initials={doctor.initials} color={doctor.color} size="small" />
          <div>
            <strong>{doctor.name}</strong>
            <small>{doctor.specialty} · {doctor.registration}</small>
            <span className={`professional-status professional-status--${doctor.status}`}>{doctor.status === 'active' ? 'Ativo' : 'Inativo'}</span>
          </div>
          {doctor.status === 'active' && <button type="button" onClick={() => setDeleteId(doctor.id)} aria-label={`Desativar ${doctor.name}`}>Desativar</button>}
        </article>
      ))}
    </section>
  )

  const reportsPane = (
    <section className="admin-appointments">
      <h2 className="section-title">Relatórios</h2>
      <div className="stats">
        <div><strong>{appointments.length}</strong><small>Total de agendamentos</small></div>
        <div><strong>{appointments.filter(({ status }) => status === 'scheduled').length}</strong><small>Agendadas</small></div>
        <div><strong>{appointments.filter(({ status }) => status === 'cancelled').length}</strong><small>Canceladas</small></div>
      </div>
      <p className="empty-message">Esses indicadores ajudam a monitorar a agenda da plataforma.</p>
    </section>
  )

  const configPane = (
    <section className="admin-appointments">
      <h2 className="section-title">Configuração</h2>
      <p>Use esta área para revisar integrações, permissões e regras de negócio.</p>
      <button className="button button--primary" type="button" onClick={() => notify('Configurações ainda não estão disponíveis nesta versão.')}>Ver configurações</button>
    </section>
  )

  return (
    <PhoneFrame label="Admin — Painel">
      <header className="admin-header">
        <div><small>Painel administrativo</small><strong>ConsultaFácil</strong></div>
        <span aria-hidden="true"><Shield /></span>
      </header>
      <main className="admin-content">
        {activeTab === 'dashboard' && dashboardPane}
        {activeTab === 'users' && usersPane}
        {activeTab === 'reports' && reportsPane}
        {activeTab === 'config' && configPane}
      </main>

      {deleteTarget && (
        <div className="overlay overlay--center">
          <section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-title">
            <h3 id="delete-title">Desativar profissional?</h3>
            <p>{deleteTarget.name} deixará de aparecer para novos agendamentos. O histórico será preservado.</p>
            <div>
              <button className="button button--muted" type="button" onClick={() => setDeleteId(null)}>Voltar</button>
              <button className="button button--danger" type="button" onClick={() => void confirmDelete()}>Desativar</button>
            </div>
          </section>
        </div>
      )}

      <BottomNav
        items={[
          { label: 'Painel', icon: LayoutDashboard, onClick: () => setActiveTab('dashboard') },
          { label: 'Usuários', icon: Users, onClick: () => setActiveTab('users') },
          { label: 'Relatórios', icon: BarChart2, onClick: () => setActiveTab('reports') },
          { label: 'Config.', icon: Settings, onClick: () => setActiveTab('config') },
        ]}
        activeLabel={
          activeTab === 'dashboard'
            ? 'Painel'
            : activeTab === 'users'
            ? 'Usuários'
            : activeTab === 'reports'
            ? 'Relatórios'
            : 'Config.'
        }
      />
    </PhoneFrame>
  )
}
