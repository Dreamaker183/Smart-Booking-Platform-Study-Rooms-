package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

public interface BookingState {
    BookingStatus getStatus();
    boolean canTransitionTo(BookingStatus newStatus);
}
