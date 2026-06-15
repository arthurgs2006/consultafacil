import { Info } from 'lucide-react'
import { useApp } from '../context/AppContext'

export function Toast() {
  const { toast } = useApp()
  return toast ? <div className="toast" role="status"><Info />{toast}</div> : null
}
