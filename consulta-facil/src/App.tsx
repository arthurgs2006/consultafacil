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

  const handleLogout = () => { logout(); setScreen('home') }

  if (screen === 'login') {
    return <AuthPhone onLogin={setScreen} />
  }

  return (
    <>
      <button className="logout-fab" onClick={handleLogout} aria-label="Sair">
        <LogOut />
      </button>
      {screen === 'patient' && <PatientPhone />}
      {screen === 'professional' && <ProfessionalPhone />}
      {screen === 'admin' && <AdminPhone />}
    </>
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
