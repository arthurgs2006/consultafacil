import { AlertCircle, ArrowLeft, CalendarHeart, Lock, LogIn, Mail } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { PhoneFrame } from '../../components/PhoneFrame'
import { useApp } from '../../context/AppContext'
import type { Role } from '../../types'

export function AuthPhone({ onLogin }: { onLogin: (role: Role) => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('maria@consultafacil.com')
  const [password, setPassword] = useState('12345678')
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

  return <PhoneFrame label="Autenticação" className="auth" fluid>{mode === 'login' ? <><header className="gradient-hero auth__hero"><CalendarHeart className="auth__logo" /><h2>Bem-vindo ao<br />ConsultaFácil</h2><p>Agende consultas com profissionais de saúde</p></header><form className="auth__body" onSubmit={submitLogin}><h3>Entrar na conta</h3><label>E-mail<div className="input-icon"><Mail /><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></div></label><label>Senha<div className="input-icon"><Lock /><input required minLength={8} type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></div></label><p className="auth__hint">Contas demo: maria@consultafacil.com, ana@consultafacil.com ou admin@consultafacil.com. Senha: 12345678.</p>{error && <div className="error-note"><AlertCircle />{error}</div>}<button disabled={submitting} className="button button--primary button--wide"><LogIn />{submitting ? 'Entrando...' : 'Entrar'}</button><p className="auth__switch">Não tem conta? <button type="button" onClick={() => { setMode('register'); setError('') }}>Cadastre-se</button></p></form></> : <><header className="gradient-hero auth__hero auth__hero--compact"><button className="back-button" onClick={() => { setMode('login'); setError('') }}><ArrowLeft />Voltar</button><h2>Criar conta</h2><p>O cadastro será salvo no backend</p></header><form className="auth__body" onSubmit={submitRegister}><label>Nome completo<input required value={name} onChange={(event) => setName(event.target.value)} /></label><label>E-mail<input required type="email" value={registerEmail} onChange={(event) => setRegisterEmail(event.target.value)} /></label><label>Senha<input required minLength={8} type="password" value={registerPassword} onChange={(event) => setRegisterPassword(event.target.value)} placeholder="Mínimo 8 caracteres" /></label><label>Papel no sistema<select value={registerRole} onChange={(event) => setRegisterRole(event.target.value as 'patient' | 'professional')}><option value="patient">Paciente</option><option value="professional">Profissional de saúde</option></select></label>{registerRole === 'professional' && <label>Especialidade<select value={specialty} onChange={(event) => setSpecialty(event.target.value)}><option>Cardiologia</option><option>Neurologia</option><option>Psicologia</option><option>Fisioterapia</option></select></label>}{error && <div className="error-note"><AlertCircle />{error}</div>}<button disabled={submitting} className="button button--primary button--wide">{submitting ? 'Criando...' : 'Criar conta'}</button></form></>}</PhoneFrame>
}
