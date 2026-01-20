package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class RejectedState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REJECTED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return newStatus == BookingStatus.CANCELLED;
    }
}
