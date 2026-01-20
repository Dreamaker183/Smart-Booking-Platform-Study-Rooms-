package com.smartbooking.persistence;

import com.smartbooking.domain.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {
    private final Database database;

    public AuditLogRepository(Database database) {
        this.database = database;
    }

    public AuditLog create(AuditLog auditLog) {
        String sql = "INSERT INTO audit_log (user_id, action, details, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, auditLog.getUserId());
            stmt.setString(2, auditLog.getAction());
            stmt.setString(3, auditLog.getDetails());
            stmt.setString(4, auditLog.getCreatedAt().toString());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new AuditLog(keys.getLong(1), auditLog.getUserId(), auditLog.getAction(),
                        auditLog.getDetails(), auditLog.getCreatedAt());
            }
            throw new SQLException("No generated key for audit log");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create audit log", ex);
        }
    }

    public List<AuditLog> findAll() {
        String sql = "SELECT * FROM audit_log ORDER BY created_at DESC";
        List<AuditLog> logs = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("action"),
                        rs.getString("details"),
                        LocalDateTime.parse(rs.getString("created_at"))
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load audit log", ex);
        }
        return logs;
    }
}
