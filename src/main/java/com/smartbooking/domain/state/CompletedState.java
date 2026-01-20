package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public class CompletedState implements BookingState {
    @Override
    public BookingStatus getStatus() {
        return BookingStatus.COMPLETED;
    }

    @Override
    public boolean canTransitionTo(BookingStatus newStatus) {
        return false;
    }
}
