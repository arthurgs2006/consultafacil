import { AlertCircle, CheckCircle2, X } from 'lucide-react'
import { useState } from 'react'
import { useApp } from '../../context/AppContext'
import { ALL_SLOTS } from '../../data/mockData'
import { formatDate } from '../../utils/date'

export function ScheduleModal({ initialDoctorId, onClose }: { initialDoctorId: number; onClose: () => void }) {
  const { doctors, getAvailability, schedule } = useApp()
  const [doctorId, setDoctorId] = useState(initialDoctorId)
  const today = new Date()
  const todayKey = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1)
  const tomorrowKey = `${tomorrow.getFullYear()}-${String(tomorrow.getMonth() + 1).padStart(2, '0')}-${String(tomorrow.getDate()).padStart(2, '0')}`
  const [date, setDate] = useState(tomorrowKey)
  const [time, setTime] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const activeDoctors = doctors.filter(({ status }) => status === 'active')
  const doctor = activeDoctors.find(({ id }) => id === doctorId)
  const { slots: availableSlots } = getAvailability({ professionalId: doctorId, date })

  const resetSelection = () => { setTime(''); setError('') }
  const [submitting, setSubmitting] = useState(false)
  const confirm = async () => {
    setSubmitting(true)
    const message = await schedule(doctorId, date, time)
    if (message) setError(message)
    else setSuccess(true)
    setSubmitting(false)
  }

  return (
    <div className="overlay overlay--bottom" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section className="sheet" role="dialog" aria-modal="true" aria-labelledby="schedule-title">
        <div className="sheet__handle" />
        <header className="sheet__header"><div><h3 id="schedule-title">Agendar consulta</h3><p>Escolha o profissional, a data e um horário livre.</p></div><button className="icon-button" onClick={onClose} aria-label="Fechar"><X /></button></header>
        {success && doctor ? <div className="success"><CheckCircle2 /><h3>Consulta agendada!</h3><p>{doctor.name} em {formatDate(date)} às {time}</p><button className="text-link" onClick={onClose}>Fechar</button></div> : <>
          <label>Profissional<select value={doctorId} onChange={(event) => { setDoctorId(Number(event.target.value)); resetSelection() }}>{activeDoctors.map((item) => <option value={item.id} key={item.id}>{item.name} — {item.specialty}</option>)}</select></label>
          {doctor && <p className="selected-professional">{doctor.registration} · avaliação {doctor.rating.toFixed(1)}</p>}
          <label>Data<input type="date" value={date} min={todayKey} onChange={(event) => { setDate(event.target.value); resetSelection() }} /></label>
          <label>Horários disponíveis</label>
          <div className="slot-grid">{ALL_SLOTS.map((slot) => { const isAvailable = availableSlots.includes(slot); const state = !isAvailable ? 'unavailable' : time === slot ? 'selected' : ''; return <button type="button" disabled={!isAvailable} className={`slot ${state}`} onClick={() => { setTime(slot); setError('') }} key={slot}>{slot}</button> })}</div>
          {!availableSlots.length && <p className="availability-empty">Não há horários disponíveis nesta data.</p>}
          {error && <div className="error-note"><AlertCircle />{error}</div>}
          <button disabled={!doctor || !date || !time || submitting} className="button button--primary button--wide" onClick={() => void confirm()}>{submitting ? 'Agendando...' : 'Confirmar agendamento'}</button>
        </>}
      </section>
    </div>
  )
}
