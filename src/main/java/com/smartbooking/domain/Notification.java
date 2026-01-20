package com.smartbooking.domain;

import java.time.LocalDateTime;

public class Notification {
    private final long id;
    private final long userId;
    private final String message;
    private final LocalDateTime createdAt;

    public Notification(long id, long userId, String message, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
