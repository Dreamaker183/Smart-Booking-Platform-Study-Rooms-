import { useMemo, useState, useRef, useEffect } from 'react'
import { format, addDays, startOfDay, addHours, isSameDay, differenceInMinutes, parseISO, isBefore, isAfter, setHours, getHours, startOfHour } from 'date-fns'
import { motion } from 'framer-motion'
import { ChevronLeft, ChevronRight } from 'lucide-react'

const START_HOUR = 8 // 8 AM
const END_HOUR = 22 // 10 PM
const DAYS_TO_SHOW = 5

function Timetable({ bookings, onSelectSlot, viewDate, onNavigate, currentUserId }) {
    const days = useMemo(() => Array.from({ length: DAYS_TO_SHOW }).map((_, i) => addDays(startOfDay(viewDate), i)), [viewDate])

    // Drag Selection State
    const [isDragging, setIsDragging] = useState(false)
    const [dragStart, setDragStart] = useState(null) // { dayIndex, hourIndex }
    const [dragEnd, setDragEnd] = useState(null)     // { dayIndex, hourIndex }
    const [isValidSelection, setIsValidSelection] = useState(true)

    // Helper to check if a specific slot is occupied
    const isSlotOccupied = (day, hour) => {
        const slotStart = setHours(day, hour)
        const slotEnd = setHours(day, hour + 1)

        return bookings.some(b => {
            // Check intersection
            // !(end <= bStart || start >= bEnd)
            // But here we are checking specific hour slots (0-minute boundaries)
            // Ideally we check if the hour block overlaps with any booking
            // For simplicity, let's assume specific slots
            // Or better: check if the booking covers this hour
            const bStart = parseISO(b.startTime)
            const bEnd = parseISO(b.endTime)

            // Check if slotStart is within [bStart, bEnd)
            // or if bStart is within [slotStart, slotEnd)
            return (
                (isAfter(slotStart, bStart) || slotStart.getTime() === bStart.getTime()) && isBefore(slotStart, bEnd)
            )
        })
    }

    const checkValidity = (start, end) => {
        // Check all hours in range
        const day = days[start.dayIndex]
        const minHour = Math.min(start.hour, end.hour)
        const maxHour = Math.max(start.hour, end.hour)

        for (let h = minHour; h <= maxHour; h++) {
            if (isSlotOccupied(day, h)) return false
        }
        return true
    }

    const handleMouseDown = (dayIndex, hour) => {
        if (isSlotOccupied(days[dayIndex], hour)) return // Prevent start on occupied

        setIsDragging(true)
        setDragStart({ dayIndex, hour })
        setDragEnd({ dayIndex, hour })
        setIsValidSelection(true) // Start valid as we checked start slot
    }

    const handleMouseEnter = (dayIndex, hour) => {
        if (!isDragging) return
        if (dayIndex !== dragStart.dayIndex) return

        setDragEnd({ dayIndex, hour })
        setIsValidSelection(checkValidity(dragStart, { dayIndex, hour }))
    }

    const handleMouseUp = () => {
        if (!isDragging || !dragStart || !dragEnd) return
        setIsDragging(false)

        if (!isValidSelection) {
            setDragStart(null)
            setDragEnd(null)
            return
        }

        // Calculate start and end times
        const day = days[dragStart.dayIndex]
        const minHour = Math.min(dragStart.hour, dragEnd.hour)
        const maxHour = Math.max(dragStart.hour, dragEnd.hour)

        const startTime = setHours(day, minHour)
        const endTime = setHours(day, maxHour + 1) // +1 because selection includes the end hour slot

        onSelectSlot(startTime, endTime)
        setDragStart(null)
        setDragEnd(null)
    }

    // Global mouse up to catch releases outside
    useEffect(() => {
        const up = () => {
            if (isDragging) handleMouseUp()
        }
        window.addEventListener('mouseup', up)
        return () => window.removeEventListener('mouseup', up)
    }, [isDragging, dragStart, dragEnd])


    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {/* Header Navigation */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button onClick={() => onNavigate(-DAYS_TO_SHOW)} style={{ padding: '8px', borderRadius: '50%', width: '36px', height: '36px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <ChevronLeft size={18} />
                    </button>
                    <button onClick={() => onNavigate(DAYS_TO_SHOW)} style={{ padding: '8px', borderRadius: '50%', width: '36px', height: '36px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <ChevronRight size={18} />
                    </button>
                </div>
                <div style={{ fontWeight: '600', color: '#4b5563' }}>
                    {format(days[0], 'MMM d')} - {format(days[days.length - 1], 'MMM d, yyyy')}
                </div>
            </div>

            <div style={{
                display: 'flex',
                border: '1px solid #e5e7eb',
                borderRadius: '16px',
                overflow: 'hidden',
                background: 'white',
                maxHeight: '400px',
                overflowY: 'auto',
                userSelect: 'none' // Prevent text selection during drag
            }}>

                {/* Time Labels Column */}
                <div style={{ width: '50px', flexShrink: 0, background: '#f9fafb', borderRight: '1px solid #e5e7eb', position: 'sticky', left: 0, zIndex: 20 }}>
                    <div style={{ height: '40px', borderBottom: '1px solid #e5e7eb', background: '#f9fafb', position: 'sticky', top: 0, zIndex: 30 }} />
                    {Array.from({ length: END_HOUR - START_HOUR }).map((_, i) => (
                        <div key={i} style={{ height: '50px', borderBottom: '1px solid #e5e7eb', fontSize: '0.65em', color: '#6b7280', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                            {i + START_HOUR}:00
                        </div>
                    ))}
                </div>

                {/* Day Columns */}
                {days.map((day, dayIndex) => (
                    <div key={day.toISOString()} style={{ flex: 1, minWidth: '100px', borderRight: dayIndex < days.length - 1 ? '1px solid #e5e7eb' : 'none', position: 'relative' }}>
                        {/* Header */}
                        <div style={{ height: '40px', borderBottom: '1px solid #e5e7eb', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', background: '#f9fafb', position: 'sticky', top: 0, zIndex: 10 }}>
                            <div style={{ fontWeight: '600', fontSize: '0.8em', color: isSameDay(day, new Date()) ? '#8b5cf6' : '#1f2937' }}>{format(day, 'EEE')}</div>
                            <div style={{ fontSize: '0.65em', color: '#6b7280' }}>{format(day, 'd MMM')}</div>
                        </div>

                        {/* Slots background */}
                        {Array.from({ length: END_HOUR - START_HOUR }).map((_, hIndex) => {
                            const hour = START_HOUR + hIndex

                            // Check if this slot is selected
                            let isSelected = false
                            if (isDragging && dragStart && dragEnd && dragStart.dayIndex === dayIndex) {
                                const min = Math.min(dragStart.hour, dragEnd.hour)
                                const max = Math.max(dragStart.hour, dragEnd.hour)
                                if (hour >= min && hour <= max) isSelected = true
                            }

                            return (
                                <div
                                    key={hour}
                                    onMouseDown={() => handleMouseDown(dayIndex, hour)}
                                    onMouseEnter={() => handleMouseEnter(dayIndex, hour)}
                                    // MouseUp is global, but we can also attach here for safety
                                    onMouseUp={handleMouseUp}
                                    style={{
                                        height: '50px',
                                        borderBottom: '1px solid #f3f4f6',
                                        cursor: isSlotOccupied(days[dayIndex], hour) ? 'not-allowed' : 'pointer', // Cursor indication
                                        background: isSelected
                                            ? (isValidSelection ? 'rgba(139, 92, 246, 0.2)' : 'rgba(239, 68, 68, 0.2)') // Red if invalid
                                            : 'transparent',
                                        transition: 'background 0.1s'
                                    }}
                                    className={!isSelected ? "timetable-slot hover:bg-gray-50 transition-colors" : ""}
                                />
                            )
                        })}

                        {/* Booking Blocks */}
                        {bookings
                            .filter(b => isSameDay(parseISO(b.startTime), day))
                            .map(b => {
                                const start = parseISO(b.startTime)
                                const end = parseISO(b.endTime)
                                // Adjust start minutes relative to the grid start (8 AM)
                                const startMinutes = (start.getHours() * 60 + start.getMinutes()) - (START_HOUR * 60)
                                const durationMinutes = differenceInMinutes(end, start)

                                const pxPerMin = 50 / 60;

                                const isMine = b.userId === currentUserId
                                // Style for "Others"
                                const otherStyle = {
                                    background: '#e5e7eb', // gray-200
                                    border: '1px solid #d1d5db', // gray-300
                                    color: '#6b7280' // gray-500
                                }
                                // Style for "Mine"
                                const myStyle = {
                                    background: b.status === 'APPROVED' || b.status === 'PAID' ? '#dcfce7' : '#fef9c3',
                                    border: b.status === 'APPROVED' || b.status === 'PAID' ? '1px solid #86efac' : '1px solid #fde047',
                                    color: b.status === 'APPROVED' || b.status === 'PAID' ? '#166534' : '#854d0e',
                                }

                                return (
                                    <motion.div
                                        key={b.id}
                                        initial={{ opacity: 0, scale: 0.9 }}
                                        animate={{ opacity: 1, scale: 1 }}
                                        style={{
                                            position: 'absolute',
                                            top: `${startMinutes * pxPerMin}px`,
                                            height: `${durationMinutes * pxPerMin}px`,
                                            left: '2px', right: '2px',
                                            marginTop: '40px', // Offset by header height
                                            borderRadius: '4px',
                                            ...(isMine ? myStyle : otherStyle),
                                            fontSize: '0.65em',
                                            padding: '2px 4px',
                                            overflow: 'hidden',
                                            zIndex: 5,
                                            pointerEvents: 'none',
                                            display: 'flex',
                                            flexDirection: 'column',
                                            justifyContent: 'center'
                                        }}
                                    >
                                        <div style={{ fontWeight: '700', lineHeight: 1.2 }}>
                                            {isMine ? b.status : (b.username || 'Occupied')}
                                        </div>
                                        {durationMinutes >= 30 && (
                                            <div style={{ opacity: 0.8, fontSize: '0.9em' }}>
                                                {format(start, 'HH:mm')} - {format(end, 'HH:mm')}
                                            </div>
                                        )}
                                    </motion.div>
                                )
                            })}
                    </div>
                ))}
            </div>
        </div>
    )
}

export default Timetable
