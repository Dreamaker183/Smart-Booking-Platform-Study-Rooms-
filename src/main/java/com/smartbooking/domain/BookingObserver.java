package com.smartbooking.domain;

public interface BookingObserver {
    void onBookingStatusChanged(Booking booking, BookingStatus oldStatus, BookingStatus newStatus);
}
