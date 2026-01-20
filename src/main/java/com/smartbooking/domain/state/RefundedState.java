package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class RefundedState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REFUNDED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return false;
    }
}
