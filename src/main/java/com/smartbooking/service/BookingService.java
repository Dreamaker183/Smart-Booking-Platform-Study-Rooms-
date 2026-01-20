package com.smartbooking.service;

import com.smartbooking.domain.Booking;
import com.smartbooking.domain.BookingStatus;
import com.smartbooking.domain.Resource;
import com.smartbooking.domain.Timeslot;
import com.smartbooking.domain.policy.ApprovalPolicy;
import com.smartbooking.domain.policy.CancellationPolicy;
import com.smartbooking.domain.policy.PricingPolicy;
import com.smartbooking.domain.state.BookingStateFactory;
import com.smartbooking.persistence.BookingRepository;
import com.smartbooking.persistence.ResourceRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final PolicyFactory policyFactory;
    private final BookingFactory bookingFactory;
    private final BookingStateFactory stateFactory;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final AuditService auditService;

    public BookingService(BookingRepository bookingRepository,
                          ResourceRepository resourceRepository,
                          PolicyFactory policyFactory,
                          BookingFactory bookingFactory,
                          BookingStateFactory stateFactory,
                          NotificationService notificationService,
                          PaymentService paymentService,
                          AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.policyFactory = policyFactory;
        this.bookingFactory = bookingFactory;
        this.stateFactory = stateFactory;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
        this.auditService = auditService;
    }

    public Booking createBooking(long userId, long resourceId, Timeslot timeslot) {
        if (timeslot.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        if (!bookingRepository.findOverlaps(resourceId, timeslot.getStart(), timeslot.getEnd()).isEmpty()) {
            throw new IllegalStateException("Requested timeslot conflicts with existing booking");
        }
        double hours = Duration.between(timeslot.getStart(), timeslot.getEnd()).toMinutes() / 60.0;
        double basePrice = hours * resource.getBasePricePerHour();
        PricingPolicy pricingPolicy = policyFactory.createPricingPolicy(resource.getPricingPolicyKey());
        double price = pricingPolicy.calculatePrice(resource, timeslot, basePrice);

        Booking booking = bookingFactory.create(userId, resourceId, timeslot.getStart(), timeslot.getEnd(), price, BookingStatus.REQUESTED);
        Booking saved = bookingRepository.create(booking);
        saved.addObserver(notificationService);

        ApprovalPolicy approvalPolicy = policyFactory.createApprovalPolicy(resource.getApprovalPolicyKey());
        if (!approvalPolicy.requiresApproval(resource)) {
            saved.transitionTo(BookingStatus.APPROVED, stateFactory);
            bookingRepository.updateStatus(saved.getId(), saved.getStatus());
            auditService.log(userId, "BOOKING_AUTO_APPROVED", "Booking " + saved.getId() + " auto-approved");
        } else {
            auditService.log(userId, "BOOKING_REQUESTED", "Booking " + saved.getId() + " awaiting approval");
        }
        return saved;
    }

    public void approveBooking(long adminId, long bookingId) {
        Booking booking = loadBooking(bookingId);
        booking.transitionTo(BookingStatus.APPROVED, stateFactory);
        bookingRepository.updateStatus(bookingId, booking.getStatus());
        auditService.log(adminId, "BOOKING_APPROVED", "Booking " + bookingId + " approved");
    }

    public void rejectBooking(long adminId, long bookingId) {
        Booking booking = loadBooking(bookingId);
        booking.transitionTo(BookingStatus.REJECTED, stateFactory);
        bookingRepository.updateStatus(bookingId, booking.getStatus());
        auditService.log(adminId, "BOOKING_REJECTED", "Booking " + bookingId + " rejected");
    }

    public void payBooking(long userId, long bookingId, String method) {
        Booking booking = loadBooking(bookingId);
        booking.transitionTo(BookingStatus.PAID, stateFactory);
        bookingRepository.updateStatus(bookingId, booking.getStatus());
        paymentService.recordPayment(bookingId, booking.getPrice(), method);
        auditService.log(userId, "BOOKING_PAID", "Booking " + bookingId + " paid");
    }

    public void cancelBooking(long userId, long bookingId) {
        Booking booking = loadBooking(bookingId);
        BookingStatus previous = booking.getStatus();
        booking.transitionTo(BookingStatus.CANCELLED, stateFactory);
        bookingRepository.updateStatus(bookingId, booking.getStatus());

        Resource resource = resourceRepository.findById(booking.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        CancellationPolicy cancellationPolicy = policyFactory.createCancellationPolicy(resource.getCancellationPolicyKey());
        long hoursBeforeStart = Duration.between(LocalDateTime.now(), booking.getStartTime()).toHours();
        double refundPercent = cancellationPolicy.refundPercent(hoursBeforeStart);

        if (refundPercent > 0 && (previous == BookingStatus.PAID || previous == BookingStatus.ACTIVE)) {
            booking.transitionTo(BookingStatus.REFUNDED, stateFactory);
            bookingRepository.updateStatus(bookingId, booking.getStatus());
            paymentService.recordRefund(bookingId, booking.getPrice() * refundPercent);
            auditService.log(userId, "BOOKING_REFUNDED", "Booking " + bookingId + " refunded at " + (refundPercent * 100) + "%");
        } else {
            auditService.log(userId, "BOOKING_CANCELLED", "Booking " + bookingId + " cancelled");
        }
    }

    public List<Booking> listUserBookings(long userId) {
        return bookingRepository.findByUser(userId);
    }

    public List<Booking> listPendingBookings() {
        return bookingRepository.findPendingApproval();
    }

    private Booking loadBooking(long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.addObserver(notificationService);
        return booking;
    }
}
