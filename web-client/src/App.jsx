import { useState, useEffect } from 'react'
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom'
import { Monitor, LogOut, User as UserIcon } from 'lucide-react'
import Login from './Login'
import Register from './Register'
import Dashboard from './Dashboard'

function App() {
  const [user, setUser] = useState(null)
  const navigate = useNavigate()
  const location = useLocation()
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) {
      setUser(JSON.parse(stored))
    } else if (location.pathname !== '/register') {
      navigate('/login')
    }
    setLoaded(true)
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const handleLogout = () => {
    localStorage.removeItem('user')
    setUser(null)
    navigate('/login')
  }

  if (!loaded) return null

  // If not logged in, just show content (Login/Register will handle themselves)
  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<Login setUser={setUser} />} />
        <Route path="/register" element={<Register setUser={setUser} />} />
        <Route path="*" element={<Login setUser={setUser} />} />
      </Routes>
    )
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', textAlign: 'left', background: '#f9fafb' }}>
      {/* Sidebar */}
      <aside style={{
        width: '260px',
        background: 'white',
        borderRight: '1px solid #e5e7eb',
        padding: '24px',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '4px 0 24px rgba(0,0,0,0.02)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '40px', paddingLeft: '8px' }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: '8px',
            background: 'linear-gradient(135deg, #8b5cf6, #ec4899)'
          }} />
          <span style={{ fontSize: '1.2em', fontWeight: '700', letterSpacing: '-0.02em', color: '#111827' }}>
            SmartBook
          </span>
        </div>

        <nav style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
          <NavItem icon={<Monitor size={20} />} label="Dashboard" active={true} />
        </nav>

        <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px', padding: '0 8px' }}>
            <div style={{
              width: '36px', height: '36px', borderRadius: '50%',
              background: '#f3f4f6', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <UserIcon size={18} color="#6b7280" />
            </div>
            <div style={{ overflow: 'hidden' }}>
              <div style={{ fontWeight: '500', fontSize: '0.9em', color: '#1f2937' }}>{user.username}</div>
              <div style={{ fontSize: '0.75em', color: '#6b7280' }}>{user.role}</div>
            </div>
          </div>
          <button
            onClick={handleLogout}
            style={{
              width: '100%', display: 'flex', alignItems: 'center', gap: '10px',
              justifyContent: 'center', background: '#f9fafb', border: '1px solid #e5e7eb',
              color: '#374151'
            }}
          >
            <LogOut size={16} />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main style={{ flex: 1, padding: '40px', overflowY: 'auto', height: '100vh', boxSizing: 'border-box' }}>
        <Routes>
          <Route path="/dashboard" element={<Dashboard user={user} />} />
          <Route path="/" element={<Dashboard user={user} />} />
        </Routes>
      </main>
    </div>
  )
}

function NavItem({ icon, label, active }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: '12px',
      padding: '12px 16px', borderRadius: '12px',
      background: active ? '#f5f3ff' : 'transparent',
      color: active ? '#7c3aed' : '#6b7280',
      cursor: 'pointer',
      transition: 'all 0.2s',
      fontWeight: active ? '600' : '400'
    }}>
      {icon}
      <span>{label}</span>
    </div>
  )
}

export default App
