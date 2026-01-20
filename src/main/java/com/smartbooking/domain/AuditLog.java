package com.smartbooking.domain;

import java.time.LocalDateTime;

public class AuditLog {
    private final long id;
    private final long userId;
    private final String action;
    private final String details;
    private final LocalDateTime createdAt;

    public AuditLog(long id, long userId, String action, String details, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
