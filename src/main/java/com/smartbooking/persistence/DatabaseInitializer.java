package com.smartbooking.persistence;

import com.smartbooking.domain.ResourceType;
import com.smartbooking.domain.Role;
import com.smartbooking.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInitializer {
    public void initialize(Database database) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            createTables(connection);
            seedData(connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize database", ex);
        }
    }

    private void createTables(Connection connection) throws SQLException {
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL
                );
                """);
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS resources (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    base_price REAL NOT NULL,
                    pricing_policy TEXT NOT NULL,
                    cancellation_policy TEXT NOT NULL,
                    approval_policy TEXT NOT NULL
                );
                """);
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS bookings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    resource_id INTEGER NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    price REAL NOT NULL,
                    status TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (resource_id) REFERENCES resources(id)
                );
                """);
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    method TEXT NOT NULL,
                    status TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (booking_id) REFERENCES bookings(id)
                );
                """);
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    message TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                """);
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action TEXT NOT NULL,
                    details TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                """);
    }

    private void seedData(Connection connection) throws SQLException {
        if (isSeeded(connection)) {
            return;
        }
        insertUser(connection, "admin", "admin123", Role.ADMIN);
        insertUser(connection, "alice", "password", Role.CUSTOMER);
        insertUser(connection, "bob", "password", Role.CUSTOMER);

        insertResource(connection, "Room A", ResourceType.STUDY_ROOM_SMALL, 8.0,
                "PEAK_WEEKEND", "FLEXIBLE", "AUTO");
        insertResource(connection, "Room B", ResourceType.STUDY_ROOM_SMALL, 8.0,
                "DEFAULT", "FLEXIBLE", "AUTO");
        insertResource(connection, "Room C", ResourceType.STUDY_ROOM_LARGE, 12.0,
                "PEAK_WEEKEND", "STRICT", "ADMIN_REQUIRED");
        insertResource(connection, "Room D", ResourceType.STUDY_ROOM_LARGE, 12.0,
                "DEFAULT", "STRICT", "ADMIN_REQUIRED");
        insertResource(connection, "Room E", ResourceType.STUDY_ROOM_MEDIA, 15.0,
                "PEAK_WEEKEND", "FLEXIBLE", "ADMIN_REQUIRED");
        insertResource(connection, "Room F", ResourceType.STUDY_ROOM_MEDIA, 15.0,
                "DEFAULT", "FLEXIBLE", "AUTO");
        insertResource(connection, "Room G", ResourceType.STUDY_ROOM_SILENT, 10.0,
                "WEEKEND", "STRICT", "AUTO");
        insertResource(connection, "Room H", ResourceType.STUDY_ROOM_SILENT, 10.0,
                "DEFAULT", "STRICT", "AUTO");
        insertResource(connection, "Room I", ResourceType.STUDY_ROOM_SMALL, 7.5,
                "PEAK_WEEKEND", "FLEXIBLE", "AUTO");
        insertResource(connection, "Room J", ResourceType.STUDY_ROOM_LARGE, 13.0,
                "WEEKEND", "STRICT", "ADMIN_REQUIRED");
    }

    private boolean isSeeded(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM resources")) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void insertUser(Connection connection, String username, String password, Role role) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, PasswordHasher.hash(password));
            stmt.setString(3, role.name());
            stmt.executeUpdate();
        }
    }

    private void insertResource(Connection connection, String name, ResourceType type, double basePrice,
                                String pricingPolicy, String cancellationPolicy, String approvalPolicy) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO resources (name, type, base_price, pricing_policy, cancellation_policy, approval_policy) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, type.name());
            stmt.setDouble(3, basePrice);
            stmt.setString(4, pricingPolicy);
            stmt.setString(5, cancellationPolicy);
            stmt.setString(6, approvalPolicy);
            stmt.executeUpdate();
        }
    }
}
