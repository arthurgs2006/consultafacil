import { ArrowRight, CalendarCheck2, HeartPulse, ShieldCheck, Stethoscope } from 'lucide-react'

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
          <small>Ambiente demonstrativo — nenhuma credencial real é necessária.</small>
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
    </main>
  )
}
