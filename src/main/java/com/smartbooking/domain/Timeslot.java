package com.smartbooking.domain;

import java.time.Duration;
import java.time.LocalDateTime;

public class Timeslot {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public Timeslot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Invalid timeslot");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public long durationMinutes() {
        return Duration.between(start, end).toMinutes();
    }
}
