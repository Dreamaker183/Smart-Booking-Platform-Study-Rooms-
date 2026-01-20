package com.smartbooking;

import com.smartbooking.domain.Booking;
import com.smartbooking.domain.BookingStatus;
import com.smartbooking.domain.state.BookingStateFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateTests {
    @Test
    void requestedCanApprove() {
        Booking booking = new Booking(1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                10.0, BookingStatus.REQUESTED, LocalDateTime.now());
        BookingStateFactory factory = new BookingStateFactory();
        booking.transitionTo(BookingStatus.APPROVED, factory);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void approvedCannotCompleteDirectly() {
        Booking booking = new Booking(1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                10.0, BookingStatus.APPROVED, LocalDateTime.now());
        BookingStateFactory factory = new BookingStateFactory();
        assertThrows(IllegalStateException.class, () -> booking.transitionTo(BookingStatus.COMPLETED, factory));
    }

    @Test
    void cancelledCanRefund() {
        Booking booking = new Booking(1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                10.0, BookingStatus.CANCELLED, LocalDateTime.now());
        BookingStateFactory factory = new BookingStateFactory();
        booking.transitionTo(BookingStatus.REFUNDED, factory);
        assertEquals(BookingStatus.REFUNDED, booking.getStatus());
    }
}
