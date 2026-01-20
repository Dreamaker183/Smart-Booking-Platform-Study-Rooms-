package com.smartbooking.domain;

import com.smartbooking.domain.state.BookingState;
import com.smartbooking.domain.state.BookingStateFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking implements BookingSubject {
    private final long id;
    private final long userId;
    private final long resourceId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final double price;
    private BookingStatus status;
    private final LocalDateTime createdAt;
    private final List<BookingObserver> observers = new ArrayList<>();

    public Booking(long id, long userId, long resourceId, LocalDateTime startTime,
                   LocalDateTime endTime, double price, BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getResourceId() {
        return resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getPrice() {
        return price;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void transitionTo(BookingStatus newStatus, BookingStateFactory stateFactory) {
        BookingState currentState = stateFactory.getState(status);
        if (!currentState.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Illegal booking transition: " + status + " -> " + newStatus);
        }
        BookingStatus oldStatus = status;
        status = newStatus;
        notifyObservers(oldStatus, newStatus);
    }

    @Override
    public void addObserver(BookingObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(BookingObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(BookingStatus oldStatus, BookingStatus newStatus) {
        for (BookingObserver observer : observers) {
            observer.onBookingStatusChanged(this, oldStatus, newStatus);
        }
    }
}
