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

                // 1. Study Rooms (20 items)
                for (int i = 1; i <= 5; i++) {
                        insertResource(connection, "Study Room " + i + " (Small)", ResourceType.STUDY_ROOM_SMALL, 8.0,
                                        "DEFAULT", "FLEXIBLE", "AUTO");
                }
                for (int i = 6; i <= 10; i++) {
                        insertResource(connection, "Study Room " + i + " (Large)", ResourceType.STUDY_ROOM_LARGE, 12.0,
                                        "PEAK_WEEKEND", "STRICT", "ADMIN_REQUIRED");
                }
                for (int i = 11; i <= 15; i++) {
                        insertResource(connection, "Media Room " + i, ResourceType.STUDY_ROOM_MEDIA, 15.0, "DEFAULT",
                                        "FLEXIBLE", "AUTO");
                }
                for (int i = 16; i <= 20; i++) {
                        insertResource(connection, "Silent Room " + i, ResourceType.STUDY_ROOM_SILENT, 10.0, "WEEKEND",
                                        "STRICT", "AUTO");
                }

                // 2. Equipment (15 items)
                for (int i = 1; i <= 10; i++) {
                        insertResource(connection, "MacBook Pro #" + i, ResourceType.EQUIPMENT, 5.0, "DEFAULT",
                                        "FLEXIBLE", "AUTO");
                }
                for (int i = 1; i <= 5; i++) {
                        insertResource(connection, "Sony A7S III #" + i, ResourceType.EQUIPMENT, 15.0, "PEAK_WEEKEND",
                                        "STRICT", "ADMIN_REQUIRED");
                }

                // 3. Computer Labs (15 items)
                for (int i = 1; i <= 15; i++) {
                        insertResource(connection, "Lab Station A-" + i, ResourceType.COMPUTER_LAB, 2.0, "DEFAULT",
                                        "STRICT", "ADMIN_REQUIRED");
                }

                // 4. Studios & Music Rooms (10 each)
                for (int i = 1; i <= 10; i++) {
                        insertResource(connection, "Recording Studio " + i, ResourceType.STUDIO, 30.0, "PEAK_WEEKEND",
                                        "STRICT", "ADMIN_REQUIRED");
                }
                for (int i = 1; i <= 10; i++) {
                        insertResource(connection, "Music Room " + (100 + i), ResourceType.MUSIC_ROOM, 10.0, "DEFAULT",
                                        "FLEXIBLE", "AUTO");
                }
        }

        private boolean isSeeded(Connection connection) throws SQLException {
                try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM resources")) {
                        ResultSet rs = stmt.executeQuery();
                        return rs.next() && rs.getInt(1) > 0;
                }
        }

        private void insertUser(Connection connection, String username, String password, Role role)
                        throws SQLException {
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
