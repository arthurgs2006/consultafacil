import { ChevronLeft, ChevronRight } from 'lucide-react'
import { toDateKey } from '../../utils/date'

const monthNames = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro']
const weekDays = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']

export function Calendar({ viewDate, selected, onViewChange, onSelect }: { viewDate: Date; selected: string; onViewChange: (date: Date) => void; onSelect: (date: string) => void }) {
  const year = viewDate.getFullYear()
  const month = viewDate.getMonth()
  const emptyDays = new Date(year, month, 1).getDay()
  const days = new Date(year, month + 1, 0).getDate()
  const move = (delta: number) => onViewChange(new Date(year, month + delta, 1))
  const now = new Date()
  const today = toDateKey(now.getFullYear(), now.getMonth(), now.getDate())

  return <section className="calendar card" aria-label={`Calendário de ${monthNames[month]} de ${year}`}><header><button className="icon-button" onClick={() => move(-1)} aria-label="Mês anterior"><ChevronLeft /></button><strong>{monthNames[month]} {year}</strong><button className="icon-button" onClick={() => move(1)} aria-label="Próximo mês"><ChevronRight /></button></header><div className="calendar__week">{weekDays.map((day) => <span title={day} key={day}>{day.slice(0, 1)}</span>)}</div><div className="calendar__days">{Array.from({ length: emptyDays }, (_, index) => <i aria-hidden="true" key={`empty-${index}`} />)}{Array.from({ length: days }, (_, index) => { const date = toDateKey(year, month, index + 1); return <button className={`${date === selected ? 'is-selected' : ''} ${date === today ? 'is-today' : ''}`} aria-label={`${index + 1} de ${monthNames[month]} de ${year}`} aria-pressed={date === selected} onClick={() => onSelect(date)} key={date}>{index + 1}</button> })}</div></section>
}
