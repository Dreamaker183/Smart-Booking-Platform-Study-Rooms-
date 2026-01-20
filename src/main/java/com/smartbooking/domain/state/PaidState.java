package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class PaidState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.PAID;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return newStatus == BookingStatus.ACTIVE
                || newStatus == BookingStatus.CANCELLED
                || newStatus == BookingStatus.REFUNDED;
    }
}
