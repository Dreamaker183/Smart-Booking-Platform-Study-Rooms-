package com.smartbooking.domain;

import java.time.LocalDateTime;

public class Payment {
    private final long id;
    private final long bookingId;
    private final double amount;
    private final String method;
    private final String status;
    private final LocalDateTime createdAt;

    public Payment(long id, long bookingId, double amount, String method, String status, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getBookingId() {
        return bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
