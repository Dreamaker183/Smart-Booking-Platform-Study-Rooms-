package com.smartbooking.service;

import com.smartbooking.domain.Booking;
import com.smartbooking.domain.BookingObserver;
import com.smartbooking.domain.BookingStatus;
import com.smartbooking.domain.Notification;
import com.smartbooking.persistence.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationService implements BookingObserver {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyUser(long userId, String message) {
        notificationRepository.create(new Notification(0L, userId, message, LocalDateTime.now()));
    }

    public List<Notification> getNotifications(long userId) {
        return notificationRepository.findByUser(userId);
    }

    @Override
    public void onBookingStatusChanged(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        String message = String.format("Booking %d status changed: %s -> %s", booking.getId(), oldStatus, newStatus);
        notifyUser(booking.getUserId(), message);
    }
}
