package com.smartbooking.persistence;

import com.smartbooking.domain.Resource;
import com.smartbooking.domain.ResourceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceRepository {
    private final Database database;

    public ResourceRepository(Database database) {
        this.database = database;
    }

    public List<Resource> findAll() {
        String sql = "SELECT * FROM resources";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resources.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load resources", ex);
        }
        return resources;
    }

    public Optional<Resource> findById(long id) {
        String sql = "SELECT * FROM resources WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load resource", ex);
        }
    }

    public Resource create(Resource resource) {
        String sql = "INSERT INTO resources (name, type, base_price, pricing_policy, cancellation_policy, approval_policy) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, resource.getName());
            stmt.setString(2, resource.getType().name());
            stmt.setDouble(3, resource.getBasePricePerHour());
            stmt.setString(4, resource.getPricingPolicyKey());
            stmt.setString(5, resource.getCancellationPolicyKey());
            stmt.setString(6, resource.getApprovalPolicyKey());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new Resource(keys.getLong(1), resource.getName(), resource.getType(),
                        resource.getBasePricePerHour(), resource.getPricingPolicyKey(),
                        resource.getCancellationPolicyKey(), resource.getApprovalPolicyKey());
            }
            throw new SQLException("No generated key for resource");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create resource", ex);
        }
    }

    private Resource map(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getLong("id"),
                rs.getString("name"),
                ResourceType.valueOf(rs.getString("type")),
                rs.getDouble("base_price"),
                rs.getString("pricing_policy"),
                rs.getString("cancellation_policy"),
                rs.getString("approval_policy")
        );
    }
}
