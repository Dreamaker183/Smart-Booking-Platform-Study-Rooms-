package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class RequestedState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REQUESTED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return newStatus == BookingStatus.APPROVED
                || newStatus == BookingStatus.REJECTED
                || newStatus == BookingStatus.CANCELLED;
    }
}
