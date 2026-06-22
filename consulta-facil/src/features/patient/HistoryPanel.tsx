import { AlertCircle, ArrowLeft, CalendarX2, CheckCircle2, XCircle } from 'lucide-react'
import { useState } from 'react'
import { Avatar } from '../../components/Avatar'
import { StatusBadge } from '../../components/StatusBadge'
import { useApp } from '../../context/AppContext'
import { formatDate } from '../../utils/date'

type CancelState = { id: number; status: 'confirm' | 'error' | 'success'; message?: string } | null

export function HistoryPanel({ onClose }: { onClose: () => void }) {
  const { appointments, doctors, cancelAppointment, notify } = useApp()
  const [filter, setFilter] = useState('')
  const [draftFilter, setDraftFilter] = useState('')
  const [cancellation, setCancellation] = useState<CancelState>(null)
  const items = appointments
    .filter((item) => !filter || item.date === filter)
    .sort((a, b) => `${b.date}${b.time}`.localeCompare(`${a.date}${a.time}`))
  const target = appointments.find(({ id }) => id === cancellation?.id)

  const confirmCancel = async () => {
    if (!cancellation) return
    const error = await cancelAppointment(cancellation.id)
    if (error) setCancellation({ ...cancellation, status: 'error', message: error })
    else {
      setCancellation({ ...cancellation, status: 'success' })
      notify('Consulta cancelada com sucesso.')
    }
  }

  return (
    <section className="panel" aria-labelledby="history-title">
      <header className="panel__header"><button className="icon-button" onClick={onClose} aria-label="Voltar"><ArrowLeft /></button><h2 id="history-title">Histórico de consultas</h2></header>
      <div className="filters"><input type="date" value={draftFilter} onChange={(event) => setDraftFilter(event.target.value)} aria-label="Filtrar histórico por data" /><button className="button button--primary" onClick={() => setFilter(draftFilter)}>Filtrar</button><button className="button button--muted" onClick={() => { setFilter(''); setDraftFilter('') }}>Limpar</button></div>
      <div className="panel__content">{items.length === 0 ? <div className="empty"><CalendarX2 /><p>Nenhuma consulta encontrada.</p></div> : items.map((item) => { const doctor = doctors.find(({ id }) => id === item.doctorId); return <article className="appointment-card" key={item.id}><div className="appointment-card__row"><Avatar initials={doctor?.initials ?? '--'} color={doctor?.color} /><div className="appointment-card__info"><strong>{doctor?.name ?? 'Profissional removido'}</strong><span>{doctor?.specialty}</span><small>{formatDate(item.date)} às {item.time}</small></div><StatusBadge status={item.status} /></div>{item.status === 'scheduled' && <button className="danger-link" onClick={() => setCancellation({ id: item.id, status: 'confirm' })}><XCircle />Cancelar consulta</button>}</article> })}</div>
      {cancellation && target && <div className="overlay overlay--center"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="cancel-title">
        {cancellation.status === 'success' ? <><CheckCircle2 className="feedback-icon feedback-icon--success" /><h3 id="cancel-title">Consulta cancelada</h3><p>O horário de {formatDate(target.date)} às {target.time} foi cancelado com sucesso.</p><button className="button button--primary button--wide" onClick={() => setCancellation(null)}>Fechar</button></> : <>
          {cancellation.status === 'error' && <AlertCircle className="feedback-icon feedback-icon--error" />}
          <h3 id="cancel-title">{cancellation.status === 'error' ? 'Não foi possível cancelar' : 'Cancelar consulta?'}</h3>
          <p>{cancellation.message ?? <>O cancelamento exige 24h de antecedência e respeita o limite de 3 por mês.<br />Esta ação não pode ser desfeita.</>}</p>
          <div><button className="button button--muted" onClick={() => setCancellation(null)}>{cancellation.status === 'error' ? 'Fechar' : 'Voltar'}</button>{cancellation.status === 'confirm' && <button className="button button--danger" onClick={() => void confirmCancel()}>Confirmar cancelamento</button>}</div>
        </>}
      </section></div>}
    </section>
  )
}
