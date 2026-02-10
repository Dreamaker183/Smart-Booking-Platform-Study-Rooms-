import { useState, useEffect, useMemo } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Calendar, Clock, DollarSign, CheckCircle, XCircle, AlertCircle, CreditCard, ChevronRight, LayoutGrid, FileText, User, Monitor, Mic, Camera, Music, ArrowLeft } from 'lucide-react'
import { format, addDays, startOfDay } from 'date-fns'
import { useNavigate } from 'react-router-dom'
import Timetable from './Timetable'
import { RESOURCE_CATEGORIES } from './ResourceTypeSelection'

const getResourceIcon = (type) => {
    switch (type) {
        case 'COMPUTER_LAB': return <Monitor size={20} color="#06b6d4" />
        case 'STUDIO': return <Mic size={20} color="#06b6d4" />
        case 'MUSIC_ROOM': return <Music size={20} color="#06b6d4" />
        case 'EQUIPMENT': return <Camera size={20} color="#06b6d4" />
        default: return <LayoutGrid size={20} color="#06b6d4" />
    }
}

function Dashboard({ user, resourceType }) {
    const navigate = useNavigate()
    const [allResources, setAllResources] = useState([])
    const [bookings, setBookings] = useState([])
    const [activeResourceBookings, setActiveResourceBookings] = useState([]) // For Timetable
    const [pendingBookings, setPendingBookings] = useState([])
    const [auditLogs, setAuditLogs] = useState([]) // [NEW] Audit Logs
    const [selectedResource, setSelectedResource] = useState(null)

    // Timetable State
    const [viewDate, setViewDate] = useState(new Date())

    const handleNavigate = (days) => {
        setViewDate(prev => addDays(prev, days))
    }

    // Dashboard Tabs for Admin
    const [adminTab, setAdminTab] = useState('pending') // 'pending' or 'audit'

    // Form State
    const [start, setStart] = useState('')
    const [end, setEnd] = useState('')

    const [loading, setLoading] = useState(false)
    const [msg, setMsg] = useState('')

    // Admin Edit State
    const [editingBooking, setEditingBooking] = useState(null)
    const [editStart, setEditStart] = useState('')
    const [editEnd, setEditEnd] = useState('')

    const fetchData = async () => {
        try {
            const [resRes, resBook] = await Promise.all([
                fetch('/api/resources', { headers: { 'X-User-Id': user.id } }).then(r => r.json()),
                fetch('/api/bookings/my', { headers: { 'X-User-Id': user.id } }).then(r => r.json())
            ])
            setAllResources(resRes)
            setBookings(resBook)

            if (user.role === 'ADMIN') {
                const [resPending, resAudit] = await Promise.all([
                    fetch('/api/bookings/pending', { headers: { 'X-User-Id': user.id } }).then(r => r.json()),
                    fetch('/api/audit', { headers: { 'X-User-Id': user.id } }).then(r => r.json())
                ])
                setPendingBookings(resPending)
                setAuditLogs(resAudit)
            }
        } catch (e) {
            console.error(e)
        }
    }

    // Fetch timetable data when resource changes
    useEffect(() => {
        if (!selectedResource) return
        const fetchResourceBookings = async () => {
            try {
                // Use startOfDay to ensure we get all bookings for the first day, not just from current time onwards
                const startDay = startOfDay(viewDate)
                const rangeStart = format(startDay, "yyyy-MM-dd'T'HH:mm:ss")
                const rangeEnd = format(addDays(startDay, 7), "yyyy-MM-dd'T'HH:mm:ss")

                // console.log(`Fetching bookings: /api/bookings?resourceId=${selectedResource}&start=${rangeStart}&end=${rangeEnd}`)
                const res = await fetch(`/api/bookings?resourceId=${selectedResource}&start=${rangeStart}&end=${rangeEnd}`, {
                    headers: { 'X-User-Id': user.id }
                })
                if (!res.ok) {
                    const text = await res.text()
                    console.error('Fetch bookings error:', res.status, text)
                }
                if (res.ok) {
                    const data = await res.json()
                    setActiveResourceBookings(data)
                }
            } catch (e) { console.error(e) }
        }
        fetchResourceBookings()
        // Poll every 30s?
    }, [selectedResource, bookings, viewDate]) // Re-fetch when my bookings change too

    useEffect(() => {
        fetchData()
    }, [user, resourceType]) // eslint-disable-line react-hooks/exhaustive-deps

    // Filter resources by the selected type
    const resources = useMemo(() => {
        if (!resourceType || resourceType === 'ALL') return allResources
        const category = RESOURCE_CATEGORIES.find(c => c.key === resourceType)
        if (!category) return allResources
        return allResources.filter(r => category.types.includes(r.type))
    }, [allResources, resourceType])

    // Default to first filtered resource
    useEffect(() => {
        if (resources.length > 0 && (!selectedResource || !resources.find(r => r.id === selectedResource))) {
            setSelectedResource(resources[0].id)
        }
    }, [resources]) // eslint-disable-line react-hooks/exhaustive-deps

    const currentCategory = RESOURCE_CATEGORIES.find(c => c.key === resourceType)

    const handleBooking = async (e) => {
        if (e) e.preventDefault()
        if (!selectedResource || !start || !end) return
        setLoading(true)
        setMsg('')
        try {
            const res = await fetch('/api/bookings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': user.id
                },
                body: JSON.stringify({
                    userId: user.id,
                    resourceId: selectedResource,
                    start: start + ':00',
                    end: end + ':00'
                })
            })
            if (!res.ok) throw new Error((await res.json()).title || 'Booking failed')
            setMsg('Success! Booking requested.')
            setStart('')
            setEnd('')
            fetchData() // Refresh list
        } catch (err) {
            setMsg(err.message)
        } finally {
            setLoading(false)
        }
    }

    const onSlotSelect = (slotStart, slotEnd) => {
        // Convert to local datetime string for input: YYYY-MM-DDTHH:mm
        // Use date-fns format
        setStart(format(slotStart, "yyyy-MM-dd'T'HH:mm"))
        setEnd(format(slotEnd, "yyyy-MM-dd'T'HH:mm"))
    }

    const handleAction = async (action, bookingId, body = {}) => {
        try {
            let url = `/api/bookings/${bookingId}/${action}`
            const res = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': user.id
                },
                body: Object.keys(body).length ? JSON.stringify(body) : undefined
            })
            if (!res.ok) throw new Error('Action failed')
            fetchData()
        } catch (err) {
            alert(err.message)
        }
    }

    if (user.role === 'ADMIN') {
        return (
            <div className="animate-in" style={{ maxWidth: '1200px', margin: '0 auto' }}>
                <header style={{ marginBottom: '40px', display: 'flex', justifyContent: 'space-between', alignItems: 'end' }}>
                    <div>
                        <h1 style={{ fontSize: '2.5em', marginBottom: '0.2em' }}>Admin Dashboard</h1>
                        <p style={{ color: '#666', margin: 0 }}>System Overview & Approvals</p>
                    </div>
                    <div style={{ display: 'flex', gap: '20px' }}>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '2em', fontWeight: '700', lineHeight: 1 }}>{pendingBookings.length}</div>
                            <div style={{ fontSize: '0.8em', color: '#facc15', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Pending</div>
                        </div>
                    </div>
                </header>

                {/* Admin Tabs */}
                <div style={{ display: 'flex', gap: '20px', marginBottom: '30px', borderBottom: '1px solid #e5e7eb' }}>
                    <button
                        onClick={() => setAdminTab('pending')}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            padding: '10px 20px',
                            borderBottom: adminTab === 'pending' ? '2px solid #8b5cf6' : '2px solid transparent',
                            color: adminTab === 'pending' ? '#8b5cf6' : '#6b7280',
                            cursor: 'pointer',
                            fontSize: '1em',
                            boxShadow: 'none'
                        }}
                    >
                        Pending Approvals
                    </button>
                    <button
                        onClick={() => setAdminTab('calendar')}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            padding: '10px 20px',
                            borderBottom: adminTab === 'calendar' ? '2px solid #8b5cf6' : '2px solid transparent',
                            color: adminTab === 'calendar' ? '#8b5cf6' : '#6b7280',
                            cursor: 'pointer',
                            fontSize: '1em',
                            boxShadow: 'none'
                        }}
                    >
                        <Calendar size={16} style={{ display: 'inline', marginRight: '8px' }} />
                        Calendar Manager
                    </button>
                    <button
                        onClick={() => setAdminTab('audit')}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            padding: '10px 20px',
                            borderBottom: adminTab === 'audit' ? '2px solid #8b5cf6' : '2px solid transparent',
                            color: adminTab === 'audit' ? '#8b5cf6' : '#6b7280',
                            cursor: 'pointer',
                            fontSize: '1em',
                            boxShadow: 'none'
                        }}
                    >
                        <FileText size={16} style={{ display: 'inline', marginRight: '8px' }} />
                        Audit Log
                    </button>
                </div>

                <AnimatePresence mode="wait">
                    {adminTab === 'pending' && (
                        <motion.div
                            key="pending"
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            exit={{ opacity: 0, x: 20 }}
                        >
                            {pendingBookings.length === 0 && <div style={{ color: '#666', fontStyle: 'italic', padding: '40px', textAlign: 'center', border: '1px dashed #e5e7eb', borderRadius: '12px' }}>No pending approvals.</div>}
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
                                {pendingBookings.map(b => (
                                    <BookingCard key={b.id} booking={b} isAdmin={true} onAction={handleAction} resources={resources} />
                                ))}
                            </div>
                        </motion.div>
                    )}

                    {adminTab === 'calendar' && (
                        <motion.div
                            key="calendar"
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -20 }}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                                <h3 style={{ margin: 0, color: '#111827' }}>Room Schedule Editor</h3>
                                <select
                                    value={selectedResource || ''}
                                    onChange={e => setSelectedResource(Number(e.target.value))}
                                    style={{ width: 'auto', margin: 0, minWidth: '200px' }}
                                >
                                    <optgroup label="Study Rooms">
                                        {resources.filter(r => r.type.startsWith('STUDY_ROOM')).map(r => (
                                            <option key={r.id} value={r.id}>{r.name}</option>
                                        ))}
                                    </optgroup>
                                    <optgroup label="Equipment">
                                        {resources.filter(r => r.type === 'EQUIPMENT').map(r => (
                                            <option key={r.id} value={r.id}>{r.name}</option>
                                        ))}
                                    </optgroup>
                                    <optgroup label="Labs & Studios">
                                        {resources.filter(r => !r.type.startsWith('STUDY_ROOM') && r.type !== 'EQUIPMENT').map(r => (
                                            <option key={r.id} value={r.id}>{r.name}</option>
                                        ))}
                                    </optgroup>
                                </select>
                            </div>
                            <Timetable
                                bookings={activeResourceBookings}
                                onSelectSlot={() => { }} // Admin doesn't create via drag here
                                onEditBooking={(b) => {
                                    setEditingBooking(b)
                                    setEditStart(format(parseISO(b.startTime), "yyyy-MM-dd'T'HH:mm"))
                                    setEditEnd(format(parseISO(b.endTime), "yyyy-MM-dd'T'HH:mm"))
                                }}
                                viewDate={viewDate}
                                onNavigate={handleNavigate}
                                currentUserId={user.id}
                                isAdmin={true}
                            />
                            <p style={{ textAlign: 'center', marginTop: '10px', fontSize: '0.9em', color: '#6b7280' }}>
                                Click any booking to edit or delete it.
                            </p>
                        </motion.div>
                    )}

                    {adminTab === 'audit' && (
                        <motion.div
                            key="audit"
                            initial={{ opacity: 0, x: 20 }}
                            animate={{ opacity: 1, x: 0 }}
                            exit={{ opacity: 0, x: -20 }}
                            style={{ background: 'white', borderRadius: '16px', border: '1px solid #e5e7eb', overflow: 'hidden' }}
                        >
                            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9em' }}>
                                <thead>
                                    <tr style={{ background: '#f9fafb', textAlign: 'left' }}>
                                        <th style={{ padding: '15px', fontWeight: '600', color: '#4b5563' }}>Time</th>
                                        <th style={{ padding: '15px', fontWeight: '600', color: '#4b5563' }}>User</th>
                                        <th style={{ padding: '15px', fontWeight: '600', color: '#4b5563' }}>Action</th>
                                        <th style={{ padding: '15px', fontWeight: '600', color: '#4b5563' }}>Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {auditLogs.slice().reverse().map(log => (
                                        <tr key={log.id} style={{ borderBottom: '1px solid #e5e7eb' }}>
                                            <td style={{ padding: '15px', color: '#6b7280' }}>{format(new Date(log.createdAt), 'MMM d, HH:mm:ss')}</td>
                                            <td style={{ padding: '15px' }}><span style={{ background: '#f3f4f6', padding: '2px 6px', borderRadius: '4px', fontSize: '0.85em', color: '#374151' }}>User {log.userId}</span></td>
                                            <td style={{ padding: '15px', color: '#0891b2' }}>{log.action}</td>
                                            <td style={{ padding: '15px', color: '#374151' }}>{log.details}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            {auditLogs.length === 0 && <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>No logs found.</div>}
                        </motion.div>
                    )}
                </AnimatePresence>

                <h3 style={{ marginTop: '50px', display: 'flex', alignItems: 'center', gap: '10px', color: '#111827' }}>
                    <LayoutGrid size={20} color="#06b6d4" /> System Status
                </h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
                    <div className='card' style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: '2em', fontWeight: '600' }}>{resources.length}</div>
                        <div style={{ fontSize: '0.8em', color: '#6b7280' }}>Active Rooms</div>
                    </div>
                    <div className='card' style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: '2em', fontWeight: '600' }}>{auditLogs.length}</div>
                        <div style={{ fontSize: '0.8em', color: '#6b7280' }}>Total Events Logged</div>
                    </div>
                </div>

                {/* Edit Modal */}
                <AnimatePresence>
                    {editingBooking && (
                        <div style={{
                            position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 100,
                            display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '20px'
                        }}>
                            <motion.div
                                initial={{ opacity: 0, scale: 0.9 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.9 }}
                                className="card"
                                style={{ maxWidth: '400px', width: '100%', position: 'relative' }}
                            >
                                <button
                                    onClick={() => setEditingBooking(null)}
                                    style={{ position: 'absolute', top: '15px', right: '15px', background: 'transparent', boxShadow: 'none', border: 'none', color: '#6b7280' }}
                                >
                                    <XCircle size={24} />
                                </button>
                                <h3 style={{ marginTop: 0 }}>Edit Booking #{editingBooking.id}</h3>
                                <p style={{ fontSize: '0.9em', color: '#6b7280' }}>Room: {resources.find(r => r.id === editingBooking.resourceId)?.name}</p>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', marginTop: '20px' }}>
                                    <div>
                                        <label style={{ fontSize: '0.85em', color: '#6b7280', display: 'block', marginBottom: '5px' }}>Start Time</label>
                                        <input
                                            type="datetime-local"
                                            value={editStart}
                                            onChange={e => setEditStart(e.target.value)}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ fontSize: '0.85em', color: '#6b7280', display: 'block', marginBottom: '5px' }}>End Time</label>
                                        <input
                                            type="datetime-local"
                                            value={editEnd}
                                            onChange={e => setEditEnd(e.target.value)}
                                        />
                                    </div>

                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginTop: '10px' }}>
                                        <button
                                            className="primary"
                                            onClick={async () => {
                                                try {
                                                    const res = await fetch(`/api/admin/bookings/${editingBooking.id}/update`, {
                                                        method: 'POST',
                                                        headers: {
                                                            'Content-Type': 'application/json',
                                                            'X-User-Id': user.id
                                                        },
                                                        body: JSON.stringify({
                                                            start: editStart.length === 16 ? editStart + ':00' : editStart,
                                                            end: editEnd.length === 16 ? editEnd + ':00' : editEnd
                                                        })
                                                    })
                                                    if (!res.ok) throw new Error('Update failed')
                                                    setEditingBooking(null)
                                                    fetchData()
                                                } catch (e) { alert(e.message) }
                                            }}
                                        >
                                            Save Changes
                                        </button>
                                        <button
                                            className="danger"
                                            onClick={async () => {
                                                if (!confirm("Are you sure you want to delete this booking?")) return
                                                try {
                                                    const res = await fetch(`/api/admin/bookings/${editingBooking.id}/delete`, {
                                                        method: 'POST',
                                                        headers: { 'X-User-Id': user.id }
                                                    })
                                                    if (!res.ok) throw new Error('Delete failed')
                                                    setEditingBooking(null)
                                                    fetchData()
                                                } catch (e) { alert(e.message) }
                                            }}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            </motion.div>
                        </div>
                    )}
                </AnimatePresence>
            </div>
        )
    }

    // CUSTOMER DASHBOARD
    return (
        <div className="animate-in" style={{ maxWidth: '1200px', margin: '0 auto' }}>
            {/* Back Navigation */}
            <div style={{ marginBottom: '20px' }}>
                <button
                    onClick={() => navigate('/')}
                    style={{
                        display: 'inline-flex', alignItems: 'center', gap: '8px',
                        background: 'transparent', border: 'none', boxShadow: 'none',
                        color: '#6b7280', padding: '8px 0', fontSize: '0.9em', cursor: 'pointer'
                    }}
                >
                    <ArrowLeft size={16} /> Back to Resources
                </button>
            </div>

            <header style={{ marginBottom: '40px', display: 'flex', justifyContent: 'space-between', alignItems: 'end' }}>
                <div>
                    <h1 style={{ fontSize: '2.5em', marginBottom: '0.2em' }}>
                        {currentCategory ? currentCategory.label : 'All Resources'}
                    </h1>
                    <p style={{ color: '#666', margin: 0 }}>
                        {currentCategory ? currentCategory.description : 'Browse and book all available resources.'}
                    </p>
                </div>
                <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '3em', fontWeight: '700', lineHeight: 1 }}>{bookings.filter(b => b.status !== 'CANCELLED').length}</div>
                    <div style={{ fontSize: '0.9em', color: '#666', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Active Bookings</div>
                </div>
            </header>

            {/* Timetable Section */}
            <section style={{ marginBottom: '40px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h3 style={{ display: 'flex', alignItems: 'center', gap: '10px', margin: 0, color: '#111827' }}>
                        {selectedResource ? getResourceIcon(resources.find(r => r.id === selectedResource)?.type) : <LayoutGrid size={20} color="#06b6d4" />}
                        Room Availability
                    </h3>
                    <select
                        value={selectedResource || ''}
                        onChange={e => setSelectedResource(Number(e.target.value))}
                        style={{ width: 'auto', margin: 0, minWidth: '200px' }}
                    >
                        <optgroup label="Study Rooms">
                            {resources.filter(r => r.type.startsWith('STUDY_ROOM')).map(r => (
                                <option key={r.id} value={r.id}>
                                    {r.name} — ${r.basePricePerHour}/hr
                                    {r.approvalPolicyKey === 'ADMIN_REQUIRED' ? ' (Approval Required)' : ''}
                                </option>
                            ))}
                        </optgroup>
                        <optgroup label="Equipment">
                            {resources.filter(r => r.type === 'EQUIPMENT').map(r => (
                                <option key={r.id} value={r.id}>
                                    {r.name} — ${r.basePricePerHour}/hr
                                </option>
                            ))}
                        </optgroup>
                        <optgroup label="Labs & Studios">
                            {resources.filter(r => !r.type.startsWith('STUDY_ROOM') && r.type !== 'EQUIPMENT').map(r => (
                                <option key={r.id} value={r.id}>
                                    {r.name} — ${r.basePricePerHour}/hr
                                    {r.approvalPolicyKey === 'ADMIN_REQUIRED' ? ' (Approval Required)' : ''}
                                </option>
                            ))}
                        </optgroup>
                    </select>
                </div>

                {selectedResource && (
                    <div style={{
                        display: 'flex',
                        gap: '15px',
                        marginBottom: '20px',
                        padding: '12px 20px',
                        background: '#f8fafc',
                        borderRadius: '12px',
                        border: '1px solid #e2e8f0',
                        fontSize: '0.85em'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b' }}>
                            <DollarSign size={14} color="#8b5cf6" />
                            <strong>Pricing:</strong> {
                                {
                                    'PEAK_HOURS': '1.2x during peak (18:00-22:00)',
                                    'WEEKEND': '1.15x on weekends',
                                    'PEAK_WEEKEND': 'Peak + Weekend surcharges apply',
                                    'DEFAULT': 'Standard hourly rate'
                                }[resources.find(r => r.id === selectedResource)?.pricingPolicyKey] || 'Standard pricing'
                            }
                        </div>
                        <div style={{ width: '1px', background: '#e2e8f0' }} />
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b' }}>
                            <AlertCircle size={14} color="#f59e0b" />
                            <strong>Cancellation:</strong> {
                                resources.find(r => r.id === selectedResource)?.cancellationPolicyKey === 'STRICT'
                                    ? 'No refunds allowed'
                                    : 'Flexible (Full refund if cancelled)'
                            }
                        </div>
                        <div style={{ width: '1px', background: '#e2e8f0' }} />
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b' }}>
                            <CheckCircle size={14} color="#10b981" />
                            <strong>Approval:</strong> {
                                resources.find(r => r.id === selectedResource)?.approvalPolicyKey === 'ADMIN_REQUIRED'
                                    ? 'Requires admin review'
                                    : 'Instant confirmation'
                            }
                        </div>
                    </div>
                )}

                <Timetable
                    bookings={activeResourceBookings}
                    onSelectSlot={onSlotSelect}
                    viewDate={viewDate}
                    onNavigate={handleNavigate}
                    currentUserId={user.id}
                />
                <p style={{ textAlign: 'center', marginTop: '10px', fontSize: '0.9em', color: '#6b7280' }}>
                    Drag to select time range. Use arrows to navigate weeks.
                </p>
            </section>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '30px', alignItems: 'start' }}>

                {/* Booking Form */}
                <section className="card" style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <h3 style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#111827', margin: 0 }}>
                        <Calendar size={20} color="#8b5cf6" /> New Booking
                    </h3>

                    {selectedResource && (
                        <div style={{
                            width: '100%',
                            height: '180px',
                            background: '#f1f5f9',
                            borderRadius: '12px',
                            overflow: 'hidden',
                            position: 'relative'
                        }}>
                            <img
                                src={`/resource/${resources.find(r => r.id === selectedResource)?.name}.png`}
                                alt="Room Thumbnail"
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                            />
                            <div style={{
                                display: 'none',
                                position: 'absolute',
                                inset: 0,
                                background: '#e2e8f0',
                                color: '#94a3b8',
                                justifyContent: 'center',
                                alignItems: 'center',
                                fontSize: '0.9em'
                            }}>
                                Photos coming soon
                            </div>
                        </div>
                    )}

                    {msg && (
                        <div style={{
                            padding: '10px', borderRadius: '8px', marginBottom: '15px',
                            background: msg.includes('Success') ? '#dcfce7' : '#fee2e2',
                            color: msg.includes('Success') ? '#166534' : '#991b1b',
                            border: msg.includes('Success') ? '1px solid #86efac' : '1px solid #fca5a5',
                            fontSize: '0.9em'
                        }}>
                            {msg}
                        </div>
                    )}
                    <form onSubmit={handleBooking} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <p style={{ fontSize: '0.9em', color: '#6b7280', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            Booking for: <strong style={{ color: '#111827' }}>{resources.find(r => r.id === selectedResource)?.name}</strong>
                            {resources.find(r => r.id === selectedResource)?.approvalPolicyKey === 'ADMIN_REQUIRED' && (
                                <span style={{
                                    background: '#fee2e2',
                                    color: '#b91c1c',
                                    padding: '2px 8px',
                                    borderRadius: '12px',
                                    fontSize: '0.75em',
                                    fontWeight: '600'
                                }}>
                                    Approval Required
                                </span>
                            )}
                        </p>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                            <div>
                                <label style={{ fontSize: '0.85em', color: '#6b7280', marginLeft: '4px' }}>Start Time</label>
                                <input
                                    type="datetime-local"
                                    value={start}
                                    onChange={e => setStart(e.target.value)}
                                    required
                                />
                            </div>
                            <div>
                                <label style={{ fontSize: '0.85em', color: '#6b7280', marginLeft: '4px' }}>End Time</label>
                                <input
                                    type="datetime-local"
                                    value={end}
                                    onChange={e => setEnd(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <button type="submit" className="primary" disabled={loading} style={{ marginTop: '10px' }}>
                            {loading ? 'Processing...' : 'Request Booking'}
                        </button>
                    </form>
                </section>

                {/* List Section */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {/* ... (Keep existing list logic) */}
                    {user.role === 'ADMIN' && pendingBookings.length > 0 && (
                        <section>
                            <h3 style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#ea580c' }}>
                                <AlertCircle size={20} /> Pending Actions
                            </h3>
                            <div style={{ display: 'grid', gap: '15px' }}>
                                {pendingBookings.map(b => (
                                    <BookingCard key={b.id} booking={b} isAdmin={true} onAction={handleAction} resources={resources} />
                                ))}
                            </div>
                        </section>
                    )}

                    <section>
                        <h3 style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#111827' }}>
                            <Clock size={20} color="#ec4899" /> My History
                        </h3>
                        <AnimatePresence>
                            <div style={{ display: 'grid', gap: '15px' }}>
                                {bookings
                                    .filter(b => !selectedResource || b.resourceId === parseInt(selectedResource))
                                    .slice().reverse().map(b => (
                                        <BookingCard key={b.id} booking={b} isAdmin={false} userId={user.id} onAction={handleAction} resources={allResources} />
                                    ))}
                                {bookings.filter(b => !selectedResource || b.resourceId === parseInt(selectedResource)).length === 0 &&
                                    <div style={{ color: '#666', fontStyle: 'italic' }}>No bookings found for this resource.</div>
                                }
                            </div>
                        </AnimatePresence>
                    </section>
                </div>

            </div>
        </div>
    )
}

function BookingCard({ booking, isAdmin, userId, onAction, resources }) {
    const resource = resources.find(r => r.id === booking.resourceId)

    // Status Icon Logic
    const StatusIcon = {
        'APPROVED': <CheckCircle size={16} />,
        'REQUESTED': <Clock size={16} />,
        'REJECTED': <XCircle size={16} />,
        'PAID': <DollarSign size={16} />,
        'CANCELLED': <XCircle size={16} />,
        'REFUNDED': <DollarSign size={16} />
    }[booking.status] || <AlertCircle size={16} />

    const startTime = new Date(booking.startTime)
    const endTime = new Date(booking.endTime)

    return (
        <motion.div
            layout
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            style={{
                background: 'white',
                border: '1px solid #e5e7eb',
                borderRadius: '16px',
                padding: '20px',
                display: 'flex',
                flexDirection: 'column',
                gap: '12px',
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)'
            }}
        >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontWeight: '600', fontSize: '1.1em', color: '#111827' }}>{resource?.name || 'Unknown Room'}</div>
                <div className={`status-badge status-${booking.status}`}>
                    {StatusIcon} {booking.status}
                </div>
            </div>

            <div style={{ display: 'flex', gap: '24px', fontSize: '0.9em', color: '#6b7280' }}>
                <div>
                    <div style={{ fontSize: '0.75em', color: '#9ca3af', marginBottom: '2px' }}>DATE</div>
                    {startTime.toLocaleDateString()}
                </div>
                <div>
                    <div style={{ fontSize: '0.75em', color: '#9ca3af', marginBottom: '2px' }}>TIME</div>
                    {startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - {endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </div>
                <div style={{ marginLeft: 'auto', textAlign: 'right' }}>
                    <div style={{ fontSize: '0.75em', color: '#9ca3af', marginBottom: '2px' }}>PRICE</div>
                    <span style={{ fontSize: '1.2em', color: '#111827', fontWeight: '500' }}>${booking.price.toFixed(2)}</span>
                </div>
            </div>

            {(isAdmin || (['REQUESTED', 'APPROVED'].includes(booking.status))) && (
                <div style={{ paddingTop: '15px', marginTop: '5px', borderTop: '1px solid #f3f4f6', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                    {isAdmin && booking.status === 'REQUESTED' && (
                        <>
                            <button onClick={() => onAction('approve', booking.id)} style={{ padding: '6px 12px', fontSize: '0.85em', background: '#dcfce7', color: '#166534', border: '1px solid #86efac', boxShadow: 'none' }}>Approve</button>
                            <button onClick={() => onAction('reject', booking.id)} className="danger" style={{ padding: '6px 12px', fontSize: '0.85em', boxShadow: 'none' }}>Reject</button>
                        </>
                    )}

                    {!isAdmin && booking.status === 'APPROVED' && (
                        <button className="primary" onClick={() => onAction('pay', booking.id, { userId, method: 'CREDIT_CARD' })} style={{ padding: '6px 16px', fontSize: '0.85em', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <CreditCard size={14} /> Pay Now
                        </button>
                    )}

                    {(booking.status === 'REQUESTED' || booking.status === 'APPROVED' || booking.status === 'PAID') && (
                        <button onClick={() => onAction('cancel', booking.id)} style={{ padding: '6px 12px', fontSize: '0.85em', background: 'transparent', border: '1px solid #d1d5db', color: '#6b7280', boxShadow: 'none' }}>
                            Cancel
                        </button>
                    )}
                </div>
            )}
        </motion.div>
    )
}

export default Dashboard
