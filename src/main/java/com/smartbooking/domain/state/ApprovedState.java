package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class ApprovedState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.APPROVED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return newStatus == BookingStatus.PAID
                || newStatus == BookingStatus.CANCELLED;
    }
}
