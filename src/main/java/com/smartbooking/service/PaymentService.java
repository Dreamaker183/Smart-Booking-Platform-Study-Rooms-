package com.smartbooking.service;

import com.smartbooking.domain.Payment;
import com.smartbooking.persistence.PaymentRepository;

import java.time.LocalDateTime;

public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment recordPayment(long bookingId, double amount, String method) {
        Payment payment = new Payment(0L, bookingId, amount, method, "PAID", LocalDateTime.now());
        return paymentRepository.create(payment);
    }

    public Payment recordRefund(long bookingId, double amount) {
        Payment payment = new Payment(0L, bookingId, amount, "REFUND", "REFUNDED", LocalDateTime.now());
        return paymentRepository.create(payment);
    }
}
