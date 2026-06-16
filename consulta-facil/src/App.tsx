import { useState } from 'react'
import { LogOut } from 'lucide-react'
import { Toast } from './components/Toast'
import { AppProvider, useApp } from './context/AppContext'
import { AdminPhone } from './features/admin/AdminPhone'
import { AuthPhone } from './features/auth/AuthPhone'
import { LandingPage } from './features/home/LandingPage'
import { PatientPhone } from './features/patient/PatientPhone'
import { ProfessionalPhone } from './features/professional/ProfessionalPhone'
import type { Role } from './types'
import './styles/main.scss'

type AppScreen = 'home' | 'login' | Role

function AppContent() {
  const { logout, session } = useApp()
  const [screen, setScreen] = useState<AppScreen>(() => session?.role ?? 'home')

  if (screen === 'home') {
    return <LandingPage onStart={() => setScreen('login')} />
  }

  const currentArea = screen === 'patient'
    ? <PatientPhone />
    : screen === 'professional'
      ? <ProfessionalPhone />
      : screen === 'admin'
        ? <AdminPhone />
        : <AuthPhone onLogin={setScreen} />

  return (
    <main className="app-flow">
      <header className="app-flow__header">
        <button onClick={() => { if (screen !== 'login') logout(); setScreen('home') }}>
          <LogOut />
          {screen === 'login' ? 'Voltar ao início' : 'Sair'}
        </button>
      </header>
      {currentArea}
    </main>
  )
}

function App() {
  return (
    <AppProvider>
      <AppContent />
      <Toast />
    </AppProvider>
  )
}

export default App
