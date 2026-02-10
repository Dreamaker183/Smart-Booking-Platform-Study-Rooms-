package com.smartbooking.persistence;

import com.smartbooking.domain.Booking;
import com.smartbooking.domain.BookingStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingRepository {
    private final Database database;

    public BookingRepository(Database database) {
        this.database = database;
    }

    public Booking create(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, resource_id, start_time, end_time, price, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, booking.getUserId());
            stmt.setLong(2, booking.getResourceId());
            stmt.setString(3, booking.getStartTime().toString());
            stmt.setString(4, booking.getEndTime().toString());
            stmt.setDouble(5, booking.getPrice());
            stmt.setString(6, booking.getStatus().name());
            stmt.setString(7, booking.getCreatedAt().toString());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new Booking(keys.getLong(1), booking.getUserId(), booking.getResourceId(),
                        booking.getStartTime(), booking.getEndTime(), booking.getPrice(),
                        booking.getStatus(), booking.getCreatedAt());
            }
            throw new SQLException("No generated key for booking");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create booking", ex);
        }
    }

    public void updateStatus(long bookingId, BookingStatus status) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, bookingId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update booking status", ex);
        }
    }

    public void updateTimes(long bookingId, LocalDateTime start, LocalDateTime end, double price) {
        String sql = "UPDATE bookings SET start_time = ?, end_time = ?, price = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());
            stmt.setDouble(3, price);
            stmt.setLong(4, bookingId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update booking times", ex);
        }
    }

    public void delete(long bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete booking", ex);
        }
    }

    public Optional<Booking> findById(long bookingId) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load booking", ex);
        }
    }

    public List<Booking> findByUser(long userId) {
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY created_at DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load bookings", ex);
        }
        return bookings;
    }

    public List<Booking> findPendingApproval() {
        String sql = "SELECT * FROM bookings WHERE status = ? ORDER BY created_at ASC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, BookingStatus.REQUESTED.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load pending bookings", ex);
        }
        return bookings;
    }

    public List<Booking> findOverlaps(long resourceId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM bookings WHERE resource_id = ? AND status IN (?, ?, ?, ?) "
                + "AND NOT (end_time <= ? OR start_time >= ?)";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, resourceId);
            stmt.setString(2, BookingStatus.REQUESTED.name());
            stmt.setString(3, BookingStatus.APPROVED.name());
            stmt.setString(4, BookingStatus.PAID.name());
            stmt.setString(5, BookingStatus.ACTIVE.name());
            stmt.setString(6, start.toString());
            stmt.setString(7, end.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to check conflicts", ex);
        }
        return bookings;
    }

    public List<Booking> findActiveByResource(long resourceId, LocalDateTime start, LocalDateTime end) {
        // [NEW] JOIN with users to get username
        String sql = "SELECT b.*, u.username FROM bookings b JOIN users u ON b.user_id = u.id "
                + "WHERE b.resource_id = ? AND b.status IN (?, ?, ?, ?) "
                + "AND NOT (b.end_time <= ? OR b.start_time >= ?) ORDER BY b.start_time ASC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, resourceId);
            stmt.setString(2, BookingStatus.REQUESTED.name());
            stmt.setString(3, BookingStatus.APPROVED.name());
            stmt.setString(4, BookingStatus.PAID.name());
            stmt.setString(5, BookingStatus.ACTIVE.name());
            stmt.setString(6, start.toString());
            stmt.setString(7, end.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapWithUsername(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load active bookings", ex);
        }
        return bookings;
    }

    private Booking map(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getLong("id"),
                rs.getLong("user_id"),
                null, // username not fetched in standard map
                rs.getLong("resource_id"),
                LocalDateTime.parse(rs.getString("start_time")),
                LocalDateTime.parse(rs.getString("end_time")),
                rs.getDouble("price"),
                BookingStatus.valueOf(rs.getString("status")),
                LocalDateTime.parse(rs.getString("created_at")));
    }

    private Booking mapWithUsername(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("username"), // [NEW]
                rs.getLong("resource_id"),
                LocalDateTime.parse(rs.getString("start_time")),
                LocalDateTime.parse(rs.getString("end_time")),
                rs.getDouble("price"),
                BookingStatus.valueOf(rs.getString("status")),
                LocalDateTime.parse(rs.getString("created_at")));
    }
}
