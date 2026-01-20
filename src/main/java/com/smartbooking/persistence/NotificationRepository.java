package com.smartbooking.persistence;

import com.smartbooking.domain.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private final Database database;

    public NotificationRepository(Database database) {
        this.database = database;
    }

    public Notification create(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, message, created_at) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setString(3, notification.getCreatedAt().toString());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new Notification(keys.getLong(1), notification.getUserId(),
                        notification.getMessage(), notification.getCreatedAt());
            }
            throw new SQLException("No generated key for notification");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create notification", ex);
        }
    }

    public List<Notification> findByUser(long userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("message"),
                        LocalDateTime.parse(rs.getString("created_at"))
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load notifications", ex);
        }
        return notifications;
    }
}
