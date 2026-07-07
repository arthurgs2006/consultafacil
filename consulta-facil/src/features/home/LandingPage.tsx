import { ArrowRight, BadgeCheck, BellRing, CalendarCheck2, HeartPulse, ShieldCheck, Stethoscope, Zap } from 'lucide-react'

export function LandingPage({ onStart }: { onStart: () => void }) {
  return (
    <main className="landing">
      <nav className="landing__nav">
        <a className="landing__brand" href="#inicio" aria-label="ConsultaFácil — início">
          <CalendarCheck2 />
          <strong>ConsultaFácil</strong>
        </a>
        <button className="button button--primary" onClick={onStart}>Acessar plataforma</button>
      </nav>

      <section className="landing__hero" id="inicio">
        <div className="landing__copy">
          <span className="landing__eyebrow"><HeartPulse /> Cuidado simples e conectado</span>
          <h1>Sua próxima consulta começa sem complicação.</h1>
          <p>Encontre profissionais, escolha o melhor horário e acompanhe seus agendamentos em um só lugar.</p>
          <button className="landing__cta" onClick={onStart}>
            Entrar no ConsultaFácil <ArrowRight />
          </button>
        </div>

        <div className="landing__visual" aria-hidden="true">
          <div className="landing__visual-card">
            <span><Stethoscope /></span>
            <div><small>Próxima consulta</small><strong>Dra. Ana Souza</strong><p>Amanhã, às 09:00</p></div>
          </div>
          <div className="landing__visual-card landing__visual-card--security">
            <span><ShieldCheck /></span>
            <div><strong>Seus dados protegidos</strong><p>Controle e praticidade para sua saúde.</p></div>
          </div>
        </div>
      </section>

      <section className="landing__benefits" aria-label="Vantagens do ConsultaFácil">
        <article>
          <span><Zap /></span>
          <strong>Agendamento rápido</strong>
          <p>Escolha profissional, data e horário em poucos toques.</p>
        </article>
        <article>
          <span><BadgeCheck /></span>
          <strong>Profissionais verificados</strong>
          <p>Especialidade e registro conferidos na plataforma.</p>
        </article>
        <article>
          <span><BellRing /></span>
          <strong>Lembretes automáticos</strong>
          <p>Você recebe um aviso antes de cada consulta.</p>
        </article>
      </section>
    </main>
  )
}
