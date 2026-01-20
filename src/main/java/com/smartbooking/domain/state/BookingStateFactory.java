package com.smartbooking.domain.state;

import com.smartbooking.domain.BookingStatus;

import java.util.EnumMap;
import java.util.Map;

public class BookingStateFactory {
    private final Map<BookingStatus, BookingState> states = new EnumMap<>(BookingStatus.class);

    public BookingStateFactory() {
        states.put(BookingStatus.REQUESTED, new RequestedState());
        states.put(BookingStatus.APPROVED, new ApprovedState());
        states.put(BookingStatus.REJECTED, new RejectedState());
        states.put(BookingStatus.PAID, new PaidState());
        states.put(BookingStatus.ACTIVE, new ActiveState());
        states.put(BookingStatus.COMPLETED, new CompletedState());
        states.put(BookingStatus.CANCELLED, new CancelledState());
        states.put(BookingStatus.REFUNDED, new RefundedState());
    }

    public BookingState getState(BookingStatus status) {
        return states.get(status);
    }
}
