package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class CancelledState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return newStatus == BookingStatus.REFUNDED;
    }
}
