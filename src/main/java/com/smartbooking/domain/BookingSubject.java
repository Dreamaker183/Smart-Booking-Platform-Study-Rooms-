package com.smartbooking.domain;

public interface BookingSubject {
    void addObserver(BookingObserver observer);
    void removeObserver(BookingObserver observer);
}
