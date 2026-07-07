import { AlertCircle, ArrowLeft, CalendarHeart, Eye, EyeOff, Lock, LogIn, Mail } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useApp } from '../../context/AppContext'
import type { Role } from '../../types'

export function AuthPhone({ onLogin, onBack }: { onLogin: (role: Role) => void; onBack: () => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [name, setName] = useState('')
  const [registerEmail, setRegisterEmail] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [registerRole, setRegisterRole] = useState<'patient' | 'professional'>('patient')
  const [specialty, setSpecialty] = useState('Cardiologia')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { login, register, notify } = useApp()

  const run = async (action: () => Promise<Role>) => {
    setSubmitting(true); setError('')
    try { const role = await action(); notify('Autenticação realizada com sucesso!'); onLogin(role) }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Não foi possível autenticar.') }
    finally { setSubmitting(false) }
  }
  const submitLogin = (event: FormEvent) => { event.preventDefault(); void run(() => login(email.trim(), password)) }
  const submitRegister = (event: FormEvent) => { event.preventDefault(); void run(() => register({ name: name.trim(), email: registerEmail.trim(), password: registerPassword, role: registerRole, specialty })) }

  return (
    <main className="app-screen auth">
      {mode === 'login' ? (
        <>
          <header className="gradient-hero auth__hero">
            <button type="button" className="back-button" onClick={onBack}>
              <ArrowLeft />Voltar
            </button>
            <CalendarHeart className="auth__logo" />
            <h2>Bem-vindo ao<br />ConsultaFácil</h2>
            <p>Agende consultas com profissionais de saúde</p>
          </header>
          <form className="auth__body" onSubmit={submitLogin}>
            <h3>Entrar na conta</h3>
            <label>
              E-mail
              <div className="input-icon"><Mail /><input required type="email" autoComplete="email" placeholder="seu@email.com" value={email} onChange={(event) => setEmail(event.target.value)} /></div>
            </label>
            <label>
              Senha
              <div className="input-icon input-icon--password">
                <Lock />
                <input required minLength={8} type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Sua senha" value={password} onChange={(event) => setPassword(event.target.value)} />
                <button type="button" className="input-icon__toggle" onClick={() => setShowPassword((value) => !value)} aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}>
                  {showPassword ? <EyeOff /> : <Eye />}
                </button>
              </div>
            </label>
            {error && <div className="error-note"><AlertCircle />{error}</div>}
            <button disabled={submitting} className="button button--primary button--wide">
              <LogIn />{submitting ? 'Entrando...' : 'Entrar'}
            </button>
            <p className="auth__switch">
              Não tem conta? <button type="button" onClick={() => { setMode('register'); setError('') }}>Cadastre-se</button>
            </p>
          </form>
        </>
      ) : (
        <>
          <header className="gradient-hero auth__hero auth__hero--compact">
            <button className="back-button" onClick={() => { setMode('login'); setError('') }}>
              <ArrowLeft />Voltar
            </button>
            <h2>Criar conta</h2>
          </header>
          <form className="auth__body" onSubmit={submitRegister}>
            <label>Nome completo<input required value={name} onChange={(event) => setName(event.target.value)} /></label>
            <label>E-mail<input required type="email" value={registerEmail} onChange={(event) => setRegisterEmail(event.target.value)} /></label>
            <label>Senha<input required minLength={8} type="password" value={registerPassword} onChange={(event) => setRegisterPassword(event.target.value)} placeholder="Mínimo 8 caracteres" /></label>
            <fieldset className="role-selector">
              <legend>Papel no sistema</legend>
              <div>
                <button type="button" className={registerRole === 'patient' ? 'is-active' : ''} onClick={() => setRegisterRole('patient')}>Paciente</button>
                <button type="button" className={registerRole === 'professional' ? 'is-active' : ''} onClick={() => setRegisterRole('professional')}>Profissional</button>
              </div>
            </fieldset>
            {registerRole === 'professional' && (
              <label>Especialidade<select value={specialty} onChange={(event) => setSpecialty(event.target.value)}><option>Cardiologia</option><option>Neurologia</option><option>Psicologia</option><option>Ortopedia</option><option>Fisioterapia</option><option>Odontologia</option></select></label>
            )}
            {error && <div className="error-note"><AlertCircle />{error}</div>}
            <button disabled={submitting} className="button button--primary button--wide">
              <LogIn />{submitting ? 'Cadastrando...' : 'Criar conta'}
            </button>
          </form>
          <div className="security-note">
            <Lock />
            <span>Seus dados são protegidos com criptografia e nunca são compartilhados.</span>
          </div>
        </>
      )}
    </main>
  )
}
