import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
    LayoutGrid, Monitor, Mic, Music, Camera, ArrowRight, BookOpen
} from 'lucide-react'

const RESOURCE_CATEGORIES = [
    {
        key: 'STUDY_ROOM',
        label: 'Study Rooms',
        description: 'Quiet spaces for focused study and group work',
        icon: BookOpen,
        gradient: 'linear-gradient(135deg, #8b5cf6, #7c3aed)',
        glow: 'rgba(139, 92, 246, 0.25)',
        types: ['STUDY_ROOM_SMALL', 'STUDY_ROOM_LARGE', 'STUDY_ROOM_MEDIA', 'STUDY_ROOM_SILENT']
    },
    {
        key: 'EQUIPMENT',
        label: 'Equipment',
        description: 'Laptops, cameras, and professional gear',
        icon: Camera,
        gradient: 'linear-gradient(135deg, #f59e0b, #d97706)',
        glow: 'rgba(245, 158, 11, 0.25)',
        types: ['EQUIPMENT']
    },
    {
        key: 'COMPUTER_LAB',
        label: 'Computer Labs',
        description: 'Workstations with specialized software',
        icon: Monitor,
        gradient: 'linear-gradient(135deg, #06b6d4, #0891b2)',
        glow: 'rgba(6, 182, 212, 0.25)',
        types: ['COMPUTER_LAB']
    },
    {
        key: 'STUDIO',
        label: 'Studios',
        description: 'Professional recording and production studios',
        icon: Mic,
        gradient: 'linear-gradient(135deg, #ec4899, #db2777)',
        glow: 'rgba(236, 72, 153, 0.25)',
        types: ['STUDIO']
    },
    {
        key: 'MUSIC_ROOM',
        label: 'Music Rooms',
        description: 'Soundproofed rooms for practice and rehearsal',
        icon: Music,
        gradient: 'linear-gradient(135deg, #10b981, #059669)',
        glow: 'rgba(16, 185, 129, 0.25)',
        types: ['MUSIC_ROOM']
    }
]

function ResourceTypeSelection({ user }) {
    const navigate = useNavigate()
    const [resourceCounts, setResourceCounts] = useState({})
    const [hoveredCard, setHoveredCard] = useState(null)

    useEffect(() => {
        const fetchCounts = async () => {
            try {
                const res = await fetch('/api/resources', {
                    headers: { 'X-User-Id': user.id }
                })
                if (res.ok) {
                    const resources = await res.json()
                    const counts = {}
                    RESOURCE_CATEGORIES.forEach(cat => {
                        counts[cat.key] = resources.filter(r => cat.types.includes(r.type)).length
                    })
                    setResourceCounts(counts)
                }
            } catch (e) {
                console.error(e)
            }
        }
        fetchCounts()
    }, [user.id])

    return (
        <div className="animate-in" style={{ maxWidth: '960px', margin: '0 auto' }}>
            <header style={{ marginBottom: '50px', textAlign: 'center' }}>
                <h1 style={{
                    fontSize: '2.8em',
                    marginBottom: '0.3em',
                    background: 'linear-gradient(135deg, #111827 0%, #6b7280 100%)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent'
                }}>
                    What would you like to book?
                </h1>
                <p style={{ color: '#6b7280', margin: 0, fontSize: '1.1em' }}>
                    Select a resource category to view availability and make a reservation.
                </p>
            </header>

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                gap: '24px'
            }}>
                {RESOURCE_CATEGORIES.map((cat, i) => {
                    const Icon = cat.icon
                    const count = resourceCounts[cat.key] || 0
                    const isHovered = hoveredCard === cat.key

                    return (
                        <motion.div
                            key={cat.key}
                            initial={{ opacity: 0, y: 30 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: i * 0.08, duration: 0.4, ease: 'easeOut' }}
                            onMouseEnter={() => setHoveredCard(cat.key)}
                            onMouseLeave={() => setHoveredCard(null)}
                            onClick={() => navigate(`/dashboard/${cat.key}`)}
                            style={{
                                background: 'white',
                                borderRadius: '20px',
                                padding: '32px 28px',
                                cursor: 'pointer',
                                border: '1px solid #e5e7eb',
                                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                                boxShadow: isHovered
                                    ? `0 20px 40px -12px ${cat.glow}, 0 0 0 1px rgba(0,0,0,0.03)`
                                    : '0 4px 6px -1px rgba(0,0,0,0.05)',
                                transform: isHovered ? 'translateY(-6px)' : 'translateY(0)',
                                position: 'relative',
                                overflow: 'hidden'
                            }}
                        >
                            {/* Gradient accent bar */}
                            <div style={{
                                position: 'absolute',
                                top: 0,
                                left: 0,
                                right: 0,
                                height: '4px',
                                background: cat.gradient,
                                opacity: isHovered ? 1 : 0.6,
                                transition: 'opacity 0.3s ease'
                            }} />

                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'space-between',
                                marginBottom: '18px'
                            }}>
                                <div style={{
                                    width: '52px',
                                    height: '52px',
                                    borderRadius: '14px',
                                    background: cat.gradient,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    boxShadow: `0 4px 12px ${cat.glow}`
                                }}>
                                    <Icon size={24} color="white" />
                                </div>

                                <motion.div
                                    animate={{ x: isHovered ? 0 : -4, opacity: isHovered ? 1 : 0.4 }}
                                    transition={{ duration: 0.2 }}
                                >
                                    <ArrowRight size={20} color="#9ca3af" />
                                </motion.div>
                            </div>

                            <h3 style={{
                                margin: '0 0 6px 0',
                                fontSize: '1.25em',
                                fontWeight: '600',
                                color: '#111827'
                            }}>
                                {cat.label}
                            </h3>

                            <p style={{
                                margin: '0 0 18px 0',
                                fontSize: '0.88em',
                                color: '#6b7280',
                                lineHeight: '1.5'
                            }}>
                                {cat.description}
                            </p>

                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px',
                                fontSize: '0.82em',
                                color: '#9ca3af',
                                fontWeight: '500'
                            }}>
                                <LayoutGrid size={14} />
                                <span>{count} {count === 1 ? 'resource' : 'resources'} available</span>
                            </div>
                        </motion.div>
                    )
                })}
            </div>

            {/* View All Button */}
            <div style={{ textAlign: 'center', marginTop: '40px' }}>
                <button
                    onClick={() => navigate('/dashboard/ALL')}
                    style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '12px 28px',
                        background: 'white',
                        border: '1px solid #e5e7eb',
                        borderRadius: '12px',
                        color: '#374151',
                        fontSize: '0.95em',
                        fontWeight: '500'
                    }}
                >
                    <LayoutGrid size={18} />
                    View All Resources
                    <ArrowRight size={16} />
                </button>
            </div>
        </div>
    )
}

export { RESOURCE_CATEGORIES }
export default ResourceTypeSelection
