package com.smartbooking.persistence;

import com.smartbooking.domain.Role;
import com.smartbooking.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public User create(String username, String passwordHash, Role role) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role.name());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new User(keys.getLong(1), username, passwordHash, role);
            }
            throw new SQLException("No generated key for user");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create user", ex);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load user", ex);
        }
    }

    public Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load user", ex);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role"))
        );
    }
}
