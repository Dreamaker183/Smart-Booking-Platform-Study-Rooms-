package com.smartbooking.service;

import com.smartbooking.domain.Booking;
import com.smartbooking.domain.BookingStatus;

import java.time.LocalDateTime;

public class BookingFactory {
    public Booking create(long userId, long resourceId, LocalDateTime start, LocalDateTime end, double price, BookingStatus status) {
        return new Booking(0L, userId, resourceId, start, end, price, status, LocalDateTime.now());
    }
}
