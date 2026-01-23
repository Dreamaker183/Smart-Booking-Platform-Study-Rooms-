import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { User, Lock, ArrowRight } from 'lucide-react'

function Login({ setUser }) {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const navigate = useNavigate()

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            })
            if (!res.ok) throw new Error('Invalid credentials')
            const user = await res.json()
            localStorage.setItem('user', JSON.stringify(user))
            setUser(user)
            navigate('/dashboard')
        } catch (err) {
            setError(err.message)
        }
    }

    return (
        <div className="auth-container">
            <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.5 }}
                className="card auth-form"
            >
                <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                    <div style={{
                        width: '50px', height: '50px', margin: '0 auto 15px', borderRadius: '12px',
                        background: 'linear-gradient(135deg, #8b5cf6, #ec4899)',
                        boxShadow: '0 10px 25px rgba(139, 92, 246, 0.4)'
                    }} />
                    <h2 style={{ fontSize: '2em', marginBottom: '5px' }}>Welcome Back</h2>
                    <p style={{ color: '#888', margin: 0 }}>Sign in to continue to SmartBook</p>
                </div>

                {error && (
                    <div style={{
                        padding: '12px', borderRadius: '10px', marginBottom: '20px',
                        background: 'rgba(239, 68, 68, 0.1)', color: '#f87171', border: '1px solid rgba(239, 68, 68, 0.2)',
                        fontSize: '0.9em', textAlign: 'center'
                    }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div style={{ position: 'relative' }}>
                        <User size={18} style={{ position: 'absolute', top: '15px', left: '16px', color: '#666' }} />
                        <input
                            placeholder="Username"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            required
                            style={{ paddingLeft: '44px' }}
                        />
                    </div>
                    <div style={{ position: 'relative' }}>
                        <Lock size={18} style={{ position: 'absolute', top: '15px', left: '16px', color: '#666' }} />
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                            style={{ paddingLeft: '44px' }}
                        />
                    </div>

                    <button type="submit" className="primary" style={{ width: '100%', marginTop: '20px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px' }}>
                        Login <ArrowRight size={16} />
                    </button>
                </form>

                <p style={{ textAlign: 'center', marginTop: '25px', color: '#666', fontSize: '0.9em' }}>
                    Don't have an account? <Link to="/register" style={{ color: '#8b5cf6', textDecoration: 'none', fontWeight: '500' }}>Register here</Link>
                </p>
            </motion.div>
        </div>
    )
}

export default Login
