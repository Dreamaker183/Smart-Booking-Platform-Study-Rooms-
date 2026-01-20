package com.smartbooking.persistence;

import com.smartbooking.domain.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentRepository {
    private final Database database;

    public PaymentRepository(Database database) {
        this.database = database;
    }

    public Payment create(Payment payment) {
        String sql = "INSERT INTO payments (booking_id, amount, method, status, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, payment.getBookingId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getMethod());
            stmt.setString(4, payment.getStatus());
            stmt.setString(5, payment.getCreatedAt().toString());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new Payment(keys.getLong(1), payment.getBookingId(), payment.getAmount(),
                        payment.getMethod(), payment.getStatus(), payment.getCreatedAt());
            }
            throw new SQLException("No generated key for payment");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create payment", ex);
        }
    }
}
