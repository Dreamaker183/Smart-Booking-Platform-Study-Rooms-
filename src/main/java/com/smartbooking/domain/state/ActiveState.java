package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class ActiveState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.ACTIVE;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return newStatus == BookingStatus.COMPLETED
                || newStatus == BookingStatus.CANCELLED;
    }
}
